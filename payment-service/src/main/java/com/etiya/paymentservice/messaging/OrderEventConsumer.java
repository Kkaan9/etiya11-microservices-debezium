package com.etiya.paymentservice.messaging;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.etiya.paymentservice.events.OrderCreatedEvent;
import com.etiya.paymentservice.services.concretes.PaymentProcessingService;

/**
 * Spring Cloud Stream consumer. The bean name {@code orderCreated} is referenced by
 * {@code spring.cloud.function.definition} and bound to the input binding
 * {@code orderCreated-in-0} (Kafka topic "order-created") in application.yml.
 */
@Configuration
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentProcessingService paymentProcessingService;

    public OrderEventConsumer(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            log.info("OrderCreated event consumed -> {}", event);
            paymentProcessingService.applyOrderCreated(event);
        };
    }
}
