package com.etiya.notificationservice.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etiya.notificationservice.entities.Notification;
import com.etiya.notificationservice.repositories.NotificationRepository;
import com.etiya.notificationservice.services.dtos.responses.GetAllNotificationsResponse;
import com.etiya.notificationservice.services.dtos.responses.GetByIdNotificationResponse;
import com.etiya.notificationservice.services.exceptions.BusinessException;

/**
 * Read-only view onto consumed notifications. This service has no write/produce surface - all
 * data here originates from Kafka events consumed by {@code NotificationEventConsumers}.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationRepository notificationRepository;

    public NotificationsController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<GetAllNotificationsResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(notification -> new GetAllNotificationsResponse(
                        notification.getId(),
                        notification.getEventType(),
                        notification.getSourceId(),
                        notification.getMessage(),
                        notification.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{id}")
    public GetByIdNotificationResponse getById(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notification not found with id: " + id));
        return new GetByIdNotificationResponse(
                notification.getId(),
                notification.getEventType(),
                notification.getSourceId(),
                notification.getMessage(),
                notification.getCreatedAt());
    }
}
