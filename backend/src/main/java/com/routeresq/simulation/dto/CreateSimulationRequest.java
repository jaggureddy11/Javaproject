package com.routeresq.simulation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateSimulationRequest {

    @NotNull(message = "Optimization run ID is required")
    private UUID optimizationRunId;

    @Min(value = 1, message = "Speed multiplier must be at least 1x")
    @Max(value = 10, message = "Speed multiplier max 10x")
    private int speedMultiplier = 5;

    public CreateSimulationRequest() {
    }

    public CreateSimulationRequest(UUID optimizationRunId, int speedMultiplier) {
        this.optimizationRunId = optimizationRunId;
        this.speedMultiplier = speedMultiplier;
    }

    public UUID getOptimizationRunId() {
        return optimizationRunId;
    }

    public void setOptimizationRunId(UUID optimizationRunId) {
        this.optimizationRunId = optimizationRunId;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(int speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }
}
