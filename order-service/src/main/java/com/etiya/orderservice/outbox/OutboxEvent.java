package com.etiya.orderservice.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * A message queued in the Transactional Outbox table (MySQL).
 *
 * <p>The business layer inserts a row here instead of publishing to Kafka directly. This table is
 * insert-only: Debezium captures each INSERT from the database's binlog (CDC) and routes
 * the {@link #payload} to the {@link #destination} Kafka topic via the Outbox Event Router SMT.
 * No application code ever reads or updates this table again after the insert - delivery state
 * lives in the Kafka Connect connector, not in this row.</p>
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Domain aggregate the event belongs to, e.g. {@code "Order"}. */
    @Column(nullable = false)
    private String aggregateType;

    /** Identifier of the aggregate instance, e.g. the order id. */
    @Column(nullable = false)
    private String aggregateId;

    /** Logical event name, e.g. {@code "OrderCreated"}. */
    @Column(nullable = false)
    private String eventType;

    /** Kafka topic the Debezium outbox router publishes this row to. */
    @Column(nullable = false)
    private String destination;

    /** Serialized (JSON) event body, relayed to the broker as-is. */
    @Column(nullable = false, length = 4000)
    private String payload;

    // Hibernate maps LocalDateTime to MySQL's DATETIME by default, which Debezium's MySQL
    // connector captures as an INT64 epoch value - exactly what the outbox EventRouter SMT
    // requires for the event timestamp field, so no explicit column type override is needed here
    // (unlike the Postgres-backed services, where TIMESTAMPTZ has to be avoided explicitly).
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Required by JPA. */
    protected OutboxEvent() {
    }

    public OutboxEvent(String aggregateType, String aggregateId, String eventType,
                       String destination, String payload, LocalDateTime createdAt) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.destination = destination;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDestination() {
        return destination;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
