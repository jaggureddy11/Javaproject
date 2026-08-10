package com.routeresq.routing.model;

import com.routeresq.fleet.model.Vehicle;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimization_run_id")
    private OptimizationRun optimizationRun;

    @NotNull(message = "Vehicle is required for a route")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes = 0;

    @NotNull(message = "Route status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteStatus status = RouteStatus.PLANNED;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<RouteStop> stops = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Route() {
    }

    public Route(OptimizationRun optimizationRun, Vehicle vehicle, Integer versionNumber, BigDecimal totalDistanceKm, Integer totalDurationMinutes, RouteStatus status, List<RouteStop> stops, Integer version) {
        this.optimizationRun = optimizationRun;
        this.vehicle = vehicle;
        if (versionNumber != null) this.versionNumber = versionNumber;
        if (totalDistanceKm != null) this.totalDistanceKm = totalDistanceKm;
        if (totalDurationMinutes != null) this.totalDurationMinutes = totalDurationMinutes;
        if (status != null) this.status = status;
        if (stops != null) this.stops = stops;
        if (version != null) this.version = version;
    }

    public OptimizationRun getOptimizationRun() {
        return optimizationRun;
    }

    public void setOptimizationRun(OptimizationRun optimizationRun) {
        this.optimizationRun = optimizationRun;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public BigDecimal getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(BigDecimal totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }

    public List<RouteStop> getStops() {
        return stops;
    }

    public void setStops(List<RouteStop> stops) {
        this.stops = stops;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void addStop(RouteStop stop) {
        stops.add(stop);
        stop.setRoute(this);
    }

    public void removeStop(RouteStop stop) {
        stops.remove(stop);
        stop.setRoute(null);
    }

    public static RouteBuilder builder() {
        return new RouteBuilder();
    }

    public static class RouteBuilder {
        private OptimizationRun optimizationRun;
        private Vehicle vehicle;
        private Integer versionNumber = 1;
        private BigDecimal totalDistanceKm = BigDecimal.ZERO;
        private Integer totalDurationMinutes = 0;
        private RouteStatus status = RouteStatus.PLANNED;
        private List<RouteStop> stops = new ArrayList<>();
        private Integer version = 0;

        public RouteBuilder optimizationRun(OptimizationRun optimizationRun) {
            this.optimizationRun = optimizationRun;
            return this;
        }

        public RouteBuilder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public RouteBuilder versionNumber(Integer versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public RouteBuilder totalDistanceKm(BigDecimal totalDistanceKm) {
            this.totalDistanceKm = totalDistanceKm;
            return this;
        }

        public RouteBuilder totalDurationMinutes(Integer totalDurationMinutes) {
            this.totalDurationMinutes = totalDurationMinutes;
            return this;
        }

        public RouteBuilder status(RouteStatus status) {
            this.status = status;
            return this;
        }

        public RouteBuilder stops(List<RouteStop> stops) {
            this.stops = stops;
            return this;
        }

        public RouteBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public Route build() {
            return new Route(optimizationRun, vehicle, versionNumber, totalDistanceKm, totalDurationMinutes, status, stops, version);
        }
    }
}
