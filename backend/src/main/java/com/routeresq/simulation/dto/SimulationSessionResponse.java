package com.routeresq.simulation.dto;

import com.routeresq.simulation.model.SimulationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SimulationSessionResponse {

    private UUID simulationId;
    private UUID optimizationRunId;
    private SimulationStatus status;
    private int speedMultiplier;
    private double simulatedCurrentTimeMinutes;
    private String simulatedClockFormatted;
    private int activeVehiclesCount;
    private int totalDeliveriesCount;
    private int completedDeliveriesCount;
    private int lateDeliveriesCount;
    private double totalDistanceTravelledKm;
    private List<SimulationVehicleStateDto> vehicleStates;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public SimulationSessionResponse() {
    }

    public SimulationSessionResponse(UUID simulationId, UUID optimizationRunId, SimulationStatus status, int speedMultiplier, double simulatedCurrentTimeMinutes, String simulatedClockFormatted, int activeVehiclesCount, int totalDeliveriesCount, int completedDeliveriesCount, int lateDeliveriesCount, double totalDistanceTravelledKm, List<SimulationVehicleStateDto> vehicleStates, Instant createdAt, Instant startedAt, Instant completedAt) {
        this.simulationId = simulationId;
        this.optimizationRunId = optimizationRunId;
        this.status = status;
        this.speedMultiplier = speedMultiplier;
        this.simulatedCurrentTimeMinutes = simulatedCurrentTimeMinutes;
        this.simulatedClockFormatted = simulatedClockFormatted;
        this.activeVehiclesCount = activeVehiclesCount;
        this.totalDeliveriesCount = totalDeliveriesCount;
        this.completedDeliveriesCount = completedDeliveriesCount;
        this.lateDeliveriesCount = lateDeliveriesCount;
        this.totalDistanceTravelledKm = totalDistanceTravelledKm;
        this.vehicleStates = vehicleStates;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public UUID getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(UUID simulationId) {
        this.simulationId = simulationId;
    }

    public UUID getOptimizationRunId() {
        return optimizationRunId;
    }

    public void setOptimizationRunId(UUID optimizationRunId) {
        this.optimizationRunId = optimizationRunId;
    }

    public SimulationStatus getStatus() {
        return status;
    }

    public void setStatus(SimulationStatus status) {
        this.status = status;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(int speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getSimulatedCurrentTimeMinutes() {
        return simulatedCurrentTimeMinutes;
    }

    public void setSimulatedCurrentTimeMinutes(double simulatedCurrentTimeMinutes) {
        this.simulatedCurrentTimeMinutes = simulatedCurrentTimeMinutes;
    }

    public String getSimulatedClockFormatted() {
        return simulatedClockFormatted;
    }

    public void setSimulatedClockFormatted(String simulatedClockFormatted) {
        this.simulatedClockFormatted = simulatedClockFormatted;
    }

    public int getActiveVehiclesCount() {
        return activeVehiclesCount;
    }

    public void setActiveVehiclesCount(int activeVehiclesCount) {
        this.activeVehiclesCount = activeVehiclesCount;
    }

    public int getTotalDeliveriesCount() {
        return totalDeliveriesCount;
    }

    public void setTotalDeliveriesCount(int totalDeliveriesCount) {
        this.totalDeliveriesCount = totalDeliveriesCount;
    }

    public int getCompletedDeliveriesCount() {
        return completedDeliveriesCount;
    }

    public void setCompletedDeliveriesCount(int completedDeliveriesCount) {
        this.completedDeliveriesCount = completedDeliveriesCount;
    }

    public int getLateDeliveriesCount() {
        return lateDeliveriesCount;
    }

    public void setLateDeliveriesCount(int lateDeliveriesCount) {
        this.lateDeliveriesCount = lateDeliveriesCount;
    }

    public double getTotalDistanceTravelledKm() {
        return totalDistanceTravelledKm;
    }

    public void setTotalDistanceTravelledKm(double totalDistanceTravelledKm) {
        this.totalDistanceTravelledKm = totalDistanceTravelledKm;
    }

    public List<SimulationVehicleStateDto> getVehicleStates() {
        return vehicleStates;
    }

    public void setVehicleStates(List<SimulationVehicleStateDto> vehicleStates) {
        this.vehicleStates = vehicleStates;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
