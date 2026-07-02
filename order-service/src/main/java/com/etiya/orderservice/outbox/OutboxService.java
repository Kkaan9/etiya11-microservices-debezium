package com.etiya.orderservice.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Writes events into the outbox table. The business layer calls {@link #record} instead of
 * publishing to Kafka directly, so the message is durably queued in the same database
 * transaction as the business change. Debezium (CDC) picks up the insert from the database's
 * write-ahead log and publishes it to Kafka - no in-process relay is involved.
 */
@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes {@code payload} to JSON and inserts an outbox row.
     *
     * @param aggregateType domain aggregate, e.g. {@code "Order"}
     * @param aggregateId   aggregate instance id, e.g. the order id
     * @param eventType     logical event name, e.g. {@code "OrderCreated"}
     * @param destination   Kafka topic the Debezium outbox router publishes this row to
     * @param payload       event body, serialized to JSON and sent as-is to the broker
     */
    public OutboxEvent record(String aggregateType, String aggregateId, String eventType,
                              String destination, Object payload) {
        OutboxEvent event = new OutboxEvent(
                aggregateType,
                aggregateId,
                eventType,
                destination,
                serialize(payload),
                LocalDateTime.now());
        return outboxRepository.save(event);
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload: " + payload, e);
        }
    }
}
