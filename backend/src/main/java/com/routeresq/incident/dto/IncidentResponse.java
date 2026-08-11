package com.routeresq.incident.dto;

import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;

import java.time.Instant;
import java.util.UUID;

public class IncidentResponse {

    private UUID id;
    private IncidentType incidentType;
    private IncidentStatus status;
    private UUID vehicleId;
    private String vehicleCode;
    private UUID orderId;
    private String orderNumber;
    private String description;
    private Instant occurredAt;
    private Instant createdAt;

    public IncidentResponse() {
    }

    public IncidentResponse(UUID id, IncidentType incidentType, IncidentStatus status, UUID vehicleId, String vehicleCode, UUID orderId, String orderNumber, String description, Instant occurredAt, Instant createdAt) {
        this.id = id;
        this.incidentType = incidentType;
        this.status = status;
        this.vehicleId = vehicleId;
        this.vehicleCode = vehicleCode;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.description = description;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
