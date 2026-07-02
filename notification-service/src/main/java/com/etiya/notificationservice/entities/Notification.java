package com.etiya.notificationservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * A persisted notification derived from a consumed Kafka event.
 *
 * <p>This service has no outbox and produces nothing - it is a terminal consumer, so unlike the
 * other services' domain entities (kept in-memory) this one is backed directly by Postgres via
 * JPA. The {@code (event_type, source_id)} unique constraint doubles as the idempotency marker
 * (mirrors the separate {@code processed_events} table used by product-service/payment-service,
 * but folded into this entity since there is no other state to keep alongside it).</p>
 */
@Entity
@Table(name = "notifications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_type", "source_id"}))
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Logical event name, e.g. {@code "OrderCreated"}. */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /** Identifier that makes the event unique within its type, e.g. the order id. */
    @Column(name = "source_id", nullable = false)
    private String sourceId;

    /** Human-readable summary of the consumed event. */
    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected Notification() {
    }

    public Notification(String eventType, String sourceId, String message, Instant createdAt) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.message = message;
        this.createdAt = createdAt;
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

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
