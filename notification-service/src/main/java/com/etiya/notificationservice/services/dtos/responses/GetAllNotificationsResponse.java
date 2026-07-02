package com.etiya.notificationservice.services.dtos.responses;

import java.time.Instant;

public class GetAllNotificationsResponse {

    private Long id;
    private String eventType;
    private String sourceId;
    private String message;
    private Instant createdAt;

    public GetAllNotificationsResponse() {
    }

    public GetAllNotificationsResponse(Long id, String eventType, String sourceId, String message, Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
