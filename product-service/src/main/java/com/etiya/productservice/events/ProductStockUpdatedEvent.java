package com.etiya.productservice.events;

/**
 * Event published to Kafka whenever an order-created event causes product stock to change.
 * Carries the resulting stock level so downstream services can react without calling back
 * into product-service.
 */
public record ProductStockUpdatedEvent(
        int productId,
        int orderId,
        int quantity,
        int remainingStock) {
}
