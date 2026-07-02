package com.etiya.productservice.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the outbox table. Debezium (CDC) is the only consumer of new rows;
 * the application never reads this table back, it only inserts via {@link OutboxService}.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
}
