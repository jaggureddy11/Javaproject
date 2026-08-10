package com.routeresq.fleet.dto;

import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.shared.dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class VehicleRequest {

    @NotBlank(message = "Vehicle code is required")
    private String vehicleCode;

    @NotNull(message = "Depot ID is required")
    private UUID depotId;

    private UUID driverId;

    @NotNull(message = "Max weight capacity is required")
    @DecimalMin(value = "0.01", message = "Max weight must be > 0")
    private BigDecimal maxWeightKg;

    @DecimalMin(value = "0.01", message = "Max volume must be > 0")
    private BigDecimal maxVolumeM3 = new BigDecimal("12.50");

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus status = VehicleStatus.IDLE;

    @Valid
    private LocationDto currentLocation;

    public VehicleRequest() {
    }

    public VehicleRequest(String vehicleCode, UUID depotId, UUID driverId, BigDecimal maxWeightKg, BigDecimal maxVolumeM3, VehicleStatus status, LocationDto currentLocation) {
        this.vehicleCode = vehicleCode;
        this.depotId = depotId;
        this.driverId = driverId;
        this.maxWeightKg = maxWeightKg;
        this.maxVolumeM3 = maxVolumeM3;
        this.status = status;
        this.currentLocation = currentLocation;
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

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
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
}
