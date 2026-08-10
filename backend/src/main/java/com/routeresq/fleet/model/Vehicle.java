package com.routeresq.fleet.model;

import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @NotNull(message = "Home depot is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @NotBlank(message = "Vehicle code is required")
    @Column(name = "vehicle_code", nullable = false, unique = true, length = 50)
    private String vehicleCode;

    @NotNull(message = "Max weight capacity is required")
    @DecimalMin(value = "0.01", message = "Max weight capacity must be greater than 0")
    @Column(name = "max_weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    @DecimalMin(value = "0.01", message = "Max volume capacity must be greater than 0")
    @Column(name = "max_volume_m3", precision = 10, scale = 2)
    private BigDecimal maxVolumeM3 = new BigDecimal("10.00");

    @NotNull(message = "Vehicle status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.IDLE;

    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Vehicle() {
    }

    public Vehicle(Depot depot, Driver driver, String vehicleCode, BigDecimal maxWeightKg, BigDecimal maxVolumeM3, VehicleStatus status, Point currentLocation, Integer version) {
        this.depot = depot;
        this.driver = driver;
        this.vehicleCode = vehicleCode;
        this.maxWeightKg = maxWeightKg;
        if (maxVolumeM3 != null) this.maxVolumeM3 = maxVolumeM3;
        if (status != null) this.status = status;
        this.currentLocation = currentLocation;
        if (version != null) this.version = version;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public BigDecimal getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(BigDecimal maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public BigDecimal getMaxVolumeM3() {
        return maxVolumeM3;
    }

    public void setMaxVolumeM3(BigDecimal maxVolumeM3) {
        this.maxVolumeM3 = maxVolumeM3;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public static class VehicleBuilder {
        private Depot depot;
        private Driver driver;
        private String vehicleCode;
        private BigDecimal maxWeightKg;
        private BigDecimal maxVolumeM3 = new BigDecimal("10.00");
        private VehicleStatus status = VehicleStatus.IDLE;
        private Point currentLocation;
        private Integer version = 0;

        public VehicleBuilder depot(Depot depot) {
            this.depot = depot;
            return this;
        }

        public VehicleBuilder driver(Driver driver) {
            this.driver = driver;
            return this;
        }

        public VehicleBuilder vehicleCode(String vehicleCode) {
            this.vehicleCode = vehicleCode;
            return this;
        }

        public VehicleBuilder maxWeightKg(BigDecimal maxWeightKg) {
            this.maxWeightKg = maxWeightKg;
            return this;
        }

        public VehicleBuilder maxVolumeM3(BigDecimal maxVolumeM3) {
            this.maxVolumeM3 = maxVolumeM3;
            return this;
        }

        public VehicleBuilder status(VehicleStatus status) {
            this.status = status;
            return this;
        }

        public VehicleBuilder currentLocation(Point currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public VehicleBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(depot, driver, vehicleCode, maxWeightKg, maxVolumeM3, status, currentLocation, version);
        }
    }
}
