package com.etiya.paymentservice.events;

import java.math.BigDecimal;

/**
 * Event published to Kafka whenever a payment is created (either automatically in reaction to an
 * OrderCreated event, or via a manual POST to the payments API).
 */
public record PaymentCreatedEvent(
        int paymentId,
        int orderId,
        int customerId,
        BigDecimal amount,
        String status) {
}
