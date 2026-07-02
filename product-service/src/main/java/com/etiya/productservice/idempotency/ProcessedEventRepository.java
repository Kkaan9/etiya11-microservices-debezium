package com.etiya.productservice.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the idempotency table.
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventTypeAndSourceId(String eventType, String sourceId);
}
