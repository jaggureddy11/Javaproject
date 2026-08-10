package com.routeresq.incident.model;

import com.routeresq.fleet.model.Vehicle;
import com.routeresq.order.model.Order;
import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "incidents")
public class Incident extends BaseEntity {

    @NotNull(message = "Incident type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 50)
    private IncidentType incidentType;

    @NotNull(message = "Incident status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IncidentStatus status = IncidentStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    public Incident() {
    }

    public Incident(IncidentType incidentType, IncidentStatus status, Vehicle vehicle, Order order, String description, Instant occurredAt) {
        this.incidentType = incidentType;
        if (status != null) this.status = status;
        this.vehicle = vehicle;
        this.order = order;
        this.description = description;
        if (occurredAt != null) this.occurredAt = occurredAt;
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

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public static IncidentBuilder builder() {
        return new IncidentBuilder();
    }

    public static class IncidentBuilder {
        private IncidentType incidentType;
        private IncidentStatus status = IncidentStatus.OPEN;
        private Vehicle vehicle;
        private Order order;
        private String description;
        private Instant occurredAt = Instant.now();

        public IncidentBuilder incidentType(IncidentType incidentType) {
            this.incidentType = incidentType;
            return this;
        }

        public IncidentBuilder status(IncidentStatus status) {
            this.status = status;
            return this;
        }

        public IncidentBuilder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public IncidentBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public IncidentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IncidentBuilder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Incident build() {
            return new Incident(incidentType, status, vehicle, order, description, occurredAt);
        }
    }
}
