package com.etiya.productservice.messaging;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.etiya.productservice.events.OrderCreatedEvent;
import com.etiya.productservice.services.concretes.ProductStockService;

/**
 * Spring Cloud Stream consumer. The bean name {@code orderCreated} is referenced by
 * {@code spring.cloud.function.definition} and bound to the input binding
 * {@code orderCreated-in-0} (Kafka topic "order-created") in application.yml.
 */
@Configuration
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProductStockService productStockService;

    public OrderEventConsumer(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            log.info("OrderCreated event consumed -> {}", event);
            productStockService.applyOrderCreated(event);
        };
    }
}
