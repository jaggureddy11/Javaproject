package com.routeresq.realtime.model;

import java.time.Instant;
import java.util.UUID;

public class RealtimeEvent<T> {

    private UUID eventId;
    private RealtimeEventType eventType;
    private String entityType;
    private String entityId;
    private Instant occurredAt;
    private long sequence;
    private UUID simulationId;
    private UUID incidentId;
    private UUID optimizationRunId;
    private T payload;

    public RealtimeEvent() {
    }

    public RealtimeEvent(UUID eventId, RealtimeEventType eventType, String entityType, String entityId, Instant occurredAt, long sequence, UUID simulationId, UUID incidentId, UUID optimizationRunId, T payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = occurredAt;
        this.sequence = sequence;
        this.simulationId = simulationId;
        this.incidentId = incidentId;
        this.optimizationRunId = optimizationRunId;
        this.payload = payload;
    }

    public static <T> RealtimeEventBuilder<T> builder() {
        return new RealtimeEventBuilder<>();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public RealtimeEventType getEventType() {
        return eventType;
    }

    public void setEventType(RealtimeEventType eventType) {
        this.eventType = eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public UUID getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(UUID simulationId) {
        this.simulationId = simulationId;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public UUID getOptimizationRunId() {
        return optimizationRunId;
    }

    public void setOptimizationRunId(UUID optimizationRunId) {
        this.optimizationRunId = optimizationRunId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public static class RealtimeEventBuilder<T> {
        private UUID eventId = UUID.randomUUID();
        private RealtimeEventType eventType;
        private String entityType;
        private String entityId;
        private Instant occurredAt = Instant.now();
        private long sequence;
        private UUID simulationId;
        private UUID incidentId;
        private UUID optimizationRunId;
        private T payload;

        public RealtimeEventBuilder<T> eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public RealtimeEventBuilder<T> eventType(RealtimeEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public RealtimeEventBuilder<T> entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public RealtimeEventBuilder<T> entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public RealtimeEventBuilder<T> occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public RealtimeEventBuilder<T> sequence(long sequence) {
            this.sequence = sequence;
            return this;
        }

        public RealtimeEventBuilder<T> simulationId(UUID simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public RealtimeEventBuilder<T> incidentId(UUID incidentId) {
            this.incidentId = incidentId;
            return this;
        }

        public RealtimeEventBuilder<T> optimizationRunId(UUID optimizationRunId) {
            this.optimizationRunId = optimizationRunId;
            return this;
        }

        public RealtimeEventBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public RealtimeEvent<T> build() {
            return new RealtimeEvent<>(eventId, eventType, entityType, entityId, occurredAt, sequence, simulationId, incidentId, optimizationRunId, payload);
        }
    }
}
