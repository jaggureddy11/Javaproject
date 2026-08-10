package com.routeresq.routing.model;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "route_stops", uniqueConstraints = {
    @UniqueConstraint(name = "uq_route_sequence", columnNames = {"route_id", "sequence_number"})
})
public class RouteStop extends BaseEntity {

    @NotNull(message = "Route relationship is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull(message = "Sequence number is required")
    @Min(value = 1, message = "Sequence number must be >= 1")
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "estimated_arrival_minutes", nullable = false)
    private Integer estimatedArrivalMinutes;

    @Column(name = "estimated_departure_minutes", nullable = false)
    private Integer estimatedDepartureMinutes;

    @NotNull(message = "Stop status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "stop_status", nullable = false, length = 30)
    private StopStatus stopStatus = StopStatus.PENDING;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public RouteStop() {
    }

    public RouteStop(Route route, Order order, Integer sequenceNumber, Integer estimatedArrivalMinutes, Integer estimatedDepartureMinutes, StopStatus stopStatus, boolean locked, Integer version) {
        this.route = route;
        this.order = order;
        this.sequenceNumber = sequenceNumber;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
        if (stopStatus != null) this.stopStatus = stopStatus;
        this.locked = locked;
        if (version != null) this.version = version;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public Integer getEstimatedDepartureMinutes() {
        return estimatedDepartureMinutes;
    }

    public void setEstimatedDepartureMinutes(Integer estimatedDepartureMinutes) {
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
    }

    public StopStatus getStopStatus() {
        return stopStatus;
    }

    public void setStopStatus(StopStatus stopStatus) {
        this.stopStatus = stopStatus;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static RouteStopBuilder builder() {
        return new RouteStopBuilder();
    }

    public static class RouteStopBuilder {
        private Route route;
        private Order order;
        private Integer sequenceNumber;
        private Integer estimatedArrivalMinutes;
        private Integer estimatedDepartureMinutes;
        private StopStatus stopStatus = StopStatus.PENDING;
        private boolean locked = false;
        private Integer version = 0;

        public RouteStopBuilder route(Route route) {
            this.route = route;
            return this;
        }

        public RouteStopBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public RouteStopBuilder sequenceNumber(Integer sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public RouteStopBuilder estimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
            this.estimatedArrivalMinutes = estimatedArrivalMinutes;
            return this;
        }

        public RouteStopBuilder estimatedDepartureMinutes(Integer estimatedDepartureMinutes) {
            this.estimatedDepartureMinutes = estimatedDepartureMinutes;
            return this;
        }

        public RouteStopBuilder stopStatus(StopStatus stopStatus) {
            this.stopStatus = stopStatus;
            return this;
        }

        public RouteStopBuilder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public RouteStopBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public RouteStop build() {
            return new RouteStop(route, order, sequenceNumber, estimatedArrivalMinutes, estimatedDepartureMinutes, stopStatus, locked, version);
        }
    }
}
