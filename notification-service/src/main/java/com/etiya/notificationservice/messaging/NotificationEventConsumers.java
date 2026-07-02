package com.etiya.notificationservice.messaging;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.etiya.notificationservice.events.OrderCreatedEvent;
import com.etiya.notificationservice.events.PaymentCreatedEvent;
import com.etiya.notificationservice.events.ProductStockUpdatedEvent;
import com.etiya.notificationservice.services.concretes.NotificationService;

/**
 * Spring Cloud Stream consumers. Bean names are referenced by
 * {@code spring.cloud.function.definition} and bound to their respective input bindings
 * ({@code orderCreated-in-0}, {@code paymentCreated-in-0}, {@code productStockUpdated-in-0}) in
 * application.yml. This service only consumes - it has no outbox and publishes nothing back.
 */
@Configuration
public class NotificationEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumers.class);

    private final NotificationService notificationService;

    public NotificationEventConsumers(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            log.info("OrderCreated event consumed -> {}", event);
            notificationService.recordOrderCreated(event);
        };
    }

    @Bean
    public Consumer<PaymentCreatedEvent> paymentCreated() {
        return event -> {
            log.info("PaymentCreated event consumed -> {}", event);
            notificationService.recordPaymentCreated(event);
        };
    }

    @Bean
    public Consumer<ProductStockUpdatedEvent> productStockUpdated() {
        return event -> {
            log.info("ProductStockUpdated event consumed -> {}", event);
            notificationService.recordProductStockUpdated(event);
        };
    }
}
