package com.etiya.notificationservice.events;

import java.math.BigDecimal;

/**
 * Event consumed from Kafka whenever payment-service records a new payment.
 * Mirrors the producer payload; deserialized by field name from the JSON message.
 */
public record PaymentCreatedEvent(
        int paymentId,
        int orderId,
        int customerId,
        BigDecimal amount,
        String status) {
}
