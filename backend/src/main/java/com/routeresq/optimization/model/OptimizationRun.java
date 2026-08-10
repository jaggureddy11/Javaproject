package com.routeresq.optimization.model;

import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "optimization_runs")
public class OptimizationRun extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false, length = 30)
    private OptimizationRunType runType = OptimizationRunType.INITIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "solver_status", nullable = false, length = 30)
    private SolverStatus solverStatus = SolverStatus.SOLVING;

    @Column(name = "hard_score")
    private Integer hardScore = 0;

    @Column(name = "soft_score")
    private Integer softScore = 0;

    @Column(name = "execution_duration_ms")
    private Integer executionDurationMs = 0;

    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes = 0;

    public OptimizationRun() {
    }

    public OptimizationRun(OptimizationRunType runType, SolverStatus solverStatus, Integer hardScore, Integer softScore, Integer executionDurationMs, BigDecimal totalDistanceKm, Integer totalDurationMinutes) {
        if (runType != null) this.runType = runType;
        if (solverStatus != null) this.solverStatus = solverStatus;
        if (hardScore != null) this.hardScore = hardScore;
        if (softScore != null) this.softScore = softScore;
        if (executionDurationMs != null) this.executionDurationMs = executionDurationMs;
        if (totalDistanceKm != null) this.totalDistanceKm = totalDistanceKm;
        if (totalDurationMinutes != null) this.totalDurationMinutes = totalDurationMinutes;
    }

    public OptimizationRunType getRunType() {
        return runType;
    }

    public void setRunType(OptimizationRunType runType) {
        this.runType = runType;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    public Integer getHardScore() {
        return hardScore;
    }

    public void setHardScore(Integer hardScore) {
        this.hardScore = hardScore;
    }

    public Integer getSoftScore() {
        return softScore;
    }

    public void setSoftScore(Integer softScore) {
        this.softScore = softScore;
    }

    public Integer getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Integer executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
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

    public static OptimizationRunBuilder builder() {
        return new OptimizationRunBuilder();
    }

    public static class OptimizationRunBuilder {
        private OptimizationRunType runType = OptimizationRunType.INITIAL;
        private SolverStatus solverStatus = SolverStatus.SOLVING;
        private Integer hardScore = 0;
        private Integer softScore = 0;
        private Integer executionDurationMs = 0;
        private BigDecimal totalDistanceKm = BigDecimal.ZERO;
        private Integer totalDurationMinutes = 0;

        public OptimizationRunBuilder runType(OptimizationRunType runType) {
            this.runType = runType;
            return this;
        }

        public OptimizationRunBuilder solverStatus(SolverStatus solverStatus) {
            this.solverStatus = solverStatus;
            return this;
        }

        public OptimizationRunBuilder hardScore(Integer hardScore) {
            this.hardScore = hardScore;
            return this;
        }

        public OptimizationRunBuilder softScore(Integer softScore) {
            this.softScore = softScore;
            return this;
        }

        public OptimizationRunBuilder executionDurationMs(Integer executionDurationMs) {
            this.executionDurationMs = executionDurationMs;
            return this;
        }

        public OptimizationRunBuilder totalDistanceKm(BigDecimal totalDistanceKm) {
            this.totalDistanceKm = totalDistanceKm;
            return this;
        }

        public OptimizationRunBuilder totalDurationMinutes(Integer totalDurationMinutes) {
            this.totalDurationMinutes = totalDurationMinutes;
            return this;
        }

        public OptimizationRun build() {
            return new OptimizationRun(runType, solverStatus, hardScore, softScore, executionDurationMs, totalDistanceKm, totalDurationMinutes);
        }
    }
}
