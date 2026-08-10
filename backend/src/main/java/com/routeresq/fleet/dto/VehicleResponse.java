package com.routeresq.fleet.dto;

import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.shared.dto.LocationDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class VehicleResponse {

    private UUID id;
    private String vehicleCode;
    private UUID depotId;
    private String depotName;
    private UUID driverId;
    private String driverName;
    private BigDecimal maxWeightKg;
    private BigDecimal maxVolumeM3;
    private VehicleStatus status;
    private LocationDto currentLocation;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    public VehicleResponse() {
    }

    public VehicleResponse(UUID id, String vehicleCode, UUID depotId, String depotName, UUID driverId, String driverName, BigDecimal maxWeightKg, BigDecimal maxVolumeM3, VehicleStatus status, LocationDto currentLocation, Integer version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.vehicleCode = vehicleCode;
        this.depotId = depotId;
        this.depotName = depotName;
        this.driverId = driverId;
        this.driverName = driverName;
        this.maxWeightKg = maxWeightKg;
        this.maxVolumeM3 = maxVolumeM3;
        this.status = status;
        this.currentLocation = currentLocation;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public UUID getDepotId() {
        return depotId;
    }

    public void setDepotId(UUID depotId) {
        this.depotId = depotId;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
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

    public LocationDto getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LocationDto currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
