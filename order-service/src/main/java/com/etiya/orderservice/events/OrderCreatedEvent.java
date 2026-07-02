package com.etiya.orderservice.events;

import java.math.BigDecimal;

/**
 * Event published to Kafka whenever a new order is created.
 * Carries the full order detail so downstream services (e.g. product-service)
 * can react without calling back into order-service.
 */
public record OrderCreatedEvent(
        int orderId,
        int customerId,
        int productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String address) {
}
