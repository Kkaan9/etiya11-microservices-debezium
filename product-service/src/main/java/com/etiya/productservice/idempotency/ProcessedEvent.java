package com.etiya.productservice.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Idempotency marker for an inbound Kafka event (H2-backed).
 *
 * <p>Before acting on a consumed event, the consumer checks whether a row for its
 * {@code (eventType, sourceId)} already exists. If it does, the event was already processed
 * (e.g. redelivered after a rebalance, or the producer's outbox retried a send the broker had
 * in fact already accepted) and is skipped. Otherwise the row is inserted in the same
 * transaction as the business effect, so a crash between the two never leaves the event marked
 * processed without its effect applied, or vice versa.</p>
 */
@Entity
@Table(name = "processed_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_type", "source_id"}))
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Logical event name, e.g. {@code "OrderCreated"}. */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /** Identifier that makes the event unique within its type, e.g. the order id. */
    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(nullable = false)
    private Instant processedAt;

    /** Required by JPA. */
    protected ProcessedEvent() {
    }

    public ProcessedEvent(String eventType, String sourceId, Instant processedAt) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
