package com.etiya.productservice.services.concretes;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etiya.productservice.entities.Product;
import com.etiya.productservice.events.OrderCreatedEvent;
import com.etiya.productservice.events.ProductStockUpdatedEvent;
import com.etiya.productservice.idempotency.ProcessedEvent;
import com.etiya.productservice.idempotency.ProcessedEventRepository;
import com.etiya.productservice.outbox.OutboxService;
import com.etiya.productservice.repositories.ProductRepository;

import static com.etiya.productservice.config.CacheConfig.PRODUCTS_CACHE;
import static com.etiya.productservice.config.CacheConfig.PRODUCTS_LIST_CACHE;

/**
 * Applies stock changes triggered by consumed order events and queues the resulting
 * ProductStockUpdated event in the Transactional Outbox.
 *
 * <p>Exposed as a separate Spring-managed bean (rather than a method on
 * {@code OrderEventConsumer}) so {@link #applyOrderCreated} is invoked through the Spring proxy
 * and {@code @Transactional} actually takes effect; self-invocation from within the same class
 * would silently bypass the proxy.</p>
 *
 * <p><b>Idempotent consumer:</b> Kafka only guarantees at-least-once delivery (the same
 * OrderCreated message can be redelivered after a rebalance, or because the order-service outbox
 * retried a send the broker had already accepted). Before applying the stock change, this service
 * checks {@link ProcessedEventRepository} for a row already recorded for the event's order id; if
 * found, the message is a duplicate and is skipped. The processed-marker row is written in the
 * same transaction as the stock update and outbox record, so a crash can never leave the event
 * marked processed without its effect applied, or vice versa.</p>
 */
@Service
public class ProductStockService {

    private static final Logger log = LoggerFactory.getLogger(ProductStockService.class);

    /** Kafka topic the Debezium outbox router publishes the ProductStockUpdated message to. */
    private static final String PRODUCT_STOCK_UPDATED_TOPIC = "product-stock-updated";

    private static final String ORDER_CREATED_EVENT_TYPE = "OrderCreated";

    private final ProductRepository productRepository;
    private final OutboxService outboxService;
    private final ProcessedEventRepository processedEventRepository;

    public ProductStockService(ProductRepository productRepository,
                               OutboxService outboxService,
                               ProcessedEventRepository processedEventRepository) {
        this.productRepository = productRepository;
        this.outboxService = outboxService;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = PRODUCTS_CACHE, key = "#event.productId()"),
            @CacheEvict(cacheNames = PRODUCTS_LIST_CACHE, allEntries = true)
    })
    public void applyOrderCreated(OrderCreatedEvent event) {
        String sourceId = String.valueOf(event.orderId());
        if (processedEventRepository.existsByEventTypeAndSourceId(ORDER_CREATED_EVENT_TYPE, sourceId)) {
            log.info("OrderCreated event for order {} already processed, skipping (idempotent)", event.orderId());
            return;
        }

        Optional<Product> productOptional = productRepository.findById(event.productId());
        if (productOptional.isEmpty()) {
            log.warn("OrderCreated event references unknown product {}, stock not updated", event.productId());
            return;
        }

        Product product = productOptional.get();
        int remainingStock = product.getStock() - event.quantity();
        product.setStock(remainingStock);
        productRepository.save(product);

        // Transactional Outbox: queue ProductStockUpdated in the outbox table instead of
        // publishing to Kafka inline. Debezium (CDC) picks up the insert and forwards it.
        outboxService.record(
                "Product",
                String.valueOf(product.getId()),
                "ProductStockUpdated",
                PRODUCT_STOCK_UPDATED_TOPIC,
                new ProductStockUpdatedEvent(
                        product.getId(),
                        event.orderId(),
                        event.quantity(),
                        remainingStock));

        processedEventRepository.save(
                new ProcessedEvent(ORDER_CREATED_EVENT_TYPE, sourceId, Instant.now()));
    }
}
