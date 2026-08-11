package com.routeresq.simulation.dto;

import java.time.Instant;
import java.util.UUID;

public class SimulationEvent {

    private String eventType;
    private UUID simulationId;
    private double simulatedCurrentTimeMinutes;
    private String simulatedClockFormatted;
    private Object payload;
    private Instant timestamp = Instant.now();

    public SimulationEvent() {
    }

    public SimulationEvent(String eventType, UUID simulationId, double simulatedCurrentTimeMinutes, String simulatedClockFormatted, Object payload) {
        this.eventType = eventType;
        this.simulationId = simulationId;
        this.simulatedCurrentTimeMinutes = simulatedCurrentTimeMinutes;
        this.simulatedClockFormatted = simulatedClockFormatted;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(UUID simulationId) {
        this.simulationId = simulationId;
    }

    public double getSimulatedCurrentTimeMinutes() {
        return simulatedCurrentTimeMinutes;
    }

    public void setSimulatedCurrentTimeMinutes(double simulatedCurrentTimeMinutes) {
        this.simulatedCurrentTimeMinutes = simulatedCurrentTimeMinutes;
    }

    public String getSimulatedClockFormatted() {
        return simulatedClockFormatted;
    }

    public void setSimulatedClockFormatted(String simulatedClockFormatted) {
        this.simulatedClockFormatted = simulatedClockFormatted;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
