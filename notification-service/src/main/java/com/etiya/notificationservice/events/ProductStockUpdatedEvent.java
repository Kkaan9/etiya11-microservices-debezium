package com.etiya.notificationservice.events;

/**
 * Event consumed from Kafka whenever product-service applies a stock change.
 * Mirrors the producer payload; deserialized by field name from the JSON message.
 */
public record ProductStockUpdatedEvent(
        int productId,
        int orderId,
        int quantity,
        int remainingStock) {
}
