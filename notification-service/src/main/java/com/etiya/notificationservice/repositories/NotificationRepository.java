package com.etiya.notificationservice.repositories;

import com.etiya.notificationservice.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the notifications table. {@code existsByEventTypeAndSourceId} is the
 * idempotency check consulted before recording a newly consumed event.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByEventTypeAndSourceId(String eventType, String sourceId);
}
