package com.etiya.paymentservice.services.concretes;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etiya.paymentservice.entities.Payment;
import com.etiya.paymentservice.events.OrderCreatedEvent;
import com.etiya.paymentservice.events.PaymentCreatedEvent;
import com.etiya.paymentservice.idempotency.ProcessedEvent;
import com.etiya.paymentservice.idempotency.ProcessedEventRepository;
import com.etiya.paymentservice.outbox.OutboxService;
import com.etiya.paymentservice.repositories.PaymentRepository;

/**
 * Applies a payment charge triggered by consumed order events and queues the resulting
 * PaymentCreated event in the Transactional Outbox.
 *
 * <p>Exposed as a separate Spring-managed bean (rather than a method on
 * {@code OrderEventConsumer}) so {@link #applyOrderCreated} is invoked through the Spring proxy
 * and {@code @Transactional} actually takes effect; self-invocation from within the same class
 * would silently bypass the proxy.</p>
 *
 * <p><b>Idempotent consumer:</b> Kafka only guarantees at-least-once delivery (the same
 * OrderCreated message can be redelivered after a rebalance, or because the order-service outbox
 * retried a send the broker had already accepted). Before charging a payment, this service
 * checks {@link ProcessedEventRepository} for a row already recorded for the event's order id; if
 * found, the message is a duplicate and is skipped. The processed-marker row is written in the
 * same transaction as the payment record and outbox record, so a crash can never leave the event
 * marked processed without its effect applied, or vice versa.</p>
 */
@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    /** Kafka topic the Debezium outbox router publishes the PaymentCreated message to. */
    private static final String PAYMENT_CREATED_TOPIC = "payment-created";

    private static final String ORDER_CREATED_EVENT_TYPE = "OrderCreated";

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentProcessingService(PaymentRepository paymentRepository,
                                    OutboxService outboxService,
                                    ProcessedEventRepository processedEventRepository) {
        this.paymentRepository = paymentRepository;
        this.outboxService = outboxService;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void applyOrderCreated(OrderCreatedEvent event) {
        String sourceId = String.valueOf(event.orderId());
        if (processedEventRepository.existsByEventTypeAndSourceId(ORDER_CREATED_EVENT_TYPE, sourceId)) {
            log.info("OrderCreated event for order {} already processed, skipping (idempotent)", event.orderId());
            return;
        }

        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setCustomerId(event.customerId());
        payment.setAmount(event.totalPrice());
        payment.setStatus("COMPLETED");

        Payment saved = paymentRepository.save(payment);

        // Transactional Outbox: queue PaymentCreated in the outbox table instead of publishing
        // to Kafka inline. Debezium (CDC) picks up the insert and forwards it.
        outboxService.record(
                "Payment",
                String.valueOf(saved.getId()),
                "PaymentCreated",
                PAYMENT_CREATED_TOPIC,
                new PaymentCreatedEvent(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getCustomerId(),
                        saved.getAmount(),
                        saved.getStatus()));

        processedEventRepository.save(
                new ProcessedEvent(ORDER_CREATED_EVENT_TYPE, sourceId, Instant.now()));
    }
}
