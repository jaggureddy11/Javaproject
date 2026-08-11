package com.routeresq.incident.dto;

import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateIncidentRequest {

    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    private IncidentStatus status = IncidentStatus.OPEN;

    private UUID vehicleId;

    private UUID orderId;

    private String description;

    public CreateIncidentRequest() {
    }

    public CreateIncidentRequest(IncidentType incidentType, IncidentStatus status, UUID vehicleId, UUID orderId, String description) {
        this.incidentType = incidentType;
        if (status != null) this.status = status;
        this.vehicleId = vehicleId;
        this.orderId = orderId;
        this.description = description;
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

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
