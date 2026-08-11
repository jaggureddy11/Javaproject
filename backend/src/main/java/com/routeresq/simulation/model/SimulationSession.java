package com.routeresq.simulation.model;

import com.routeresq.optimization.model.OptimizationRun;
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
@Table(name = "simulation_sessions")
public class SimulationSession extends BaseEntity {

    @NotNull(message = "Optimization run is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimization_run_id", nullable = false)
    private OptimizationRun optimizationRun;

    @NotNull(message = "Simulation status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SimulationStatus status = SimulationStatus.CREATED;

    @Column(name = "speed_multiplier", nullable = false)
    private int speedMultiplier = 5;

    @Column(name = "simulated_current_time_minutes", nullable = false)
    private double simulatedCurrentTimeMinutes = 480.0; // Starts at 08:00 AM

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public SimulationSession() {
    }

    public SimulationSession(OptimizationRun optimizationRun, SimulationStatus status, int speedMultiplier, double simulatedCurrentTimeMinutes, Instant startedAt, Instant completedAt) {
        this.optimizationRun = optimizationRun;
        if (status != null) this.status = status;
        if (speedMultiplier > 0) this.speedMultiplier = speedMultiplier;
        if (simulatedCurrentTimeMinutes >= 0) this.simulatedCurrentTimeMinutes = simulatedCurrentTimeMinutes;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public OptimizationRun getOptimizationRun() {
        return optimizationRun;
    }

    public void setOptimizationRun(OptimizationRun optimizationRun) {
        this.optimizationRun = optimizationRun;
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
