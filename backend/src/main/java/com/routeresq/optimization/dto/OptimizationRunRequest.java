package com.routeresq.optimization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class OptimizationRunRequest {

    @NotNull(message = "Depot ID is required")
    private UUID depotId;

    private List<UUID> orderIds;
    private List<UUID> vehicleIds;

    @Min(value = 1, message = "Max solve seconds must be >= 1")
    @Max(value = 120, message = "Max solve seconds must be <= 120")
    private Integer maxSolveSeconds = 10;

    public OptimizationRunRequest() {
    }

    public OptimizationRunRequest(UUID depotId, List<UUID> orderIds, List<UUID> vehicleIds, Integer maxSolveSeconds) {
        this.depotId = depotId;
        this.orderIds = orderIds;
        this.vehicleIds = vehicleIds;
        this.maxSolveSeconds = maxSolveSeconds;
    }

    public UUID getDepotId() {
        return depotId;
    }

    public void setDepotId(UUID depotId) {
        this.depotId = depotId;
    }

    public List<UUID> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<UUID> orderIds) {
        this.orderIds = orderIds;
    }

    public List<UUID> getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(List<UUID> vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    public Integer getMaxSolveSeconds() {
        return maxSolveSeconds;
    }

    public void setMaxSolveSeconds(Integer maxSolveSeconds) {
        this.maxSolveSeconds = maxSolveSeconds;
    }
}
