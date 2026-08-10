package com.routeresq.optimization.dto;

import com.routeresq.optimization.model.SolverStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OptimizationRunResponse {

    private UUID optimizationRunId;
    private SolverStatus status;
    private String failureReason;
    private ScoreDto score;
    private OptimizationMetricsDto metrics;
    private List<RouteResultDto> routes;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;

    public OptimizationRunResponse() {
    }

    public OptimizationRunResponse(UUID optimizationRunId, SolverStatus status, String failureReason, ScoreDto score, OptimizationMetricsDto metrics, List<RouteResultDto> routes, Instant startedAt, Instant completedAt, Long durationMs) {
        this.optimizationRunId = optimizationRunId;
        this.status = status;
        this.failureReason = failureReason;
        this.score = score;
        this.metrics = metrics;
        this.routes = routes;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.durationMs = durationMs;
    }

    public UUID getOptimizationRunId() {
        return optimizationRunId;
    }

    public void setOptimizationRunId(UUID optimizationRunId) {
        this.optimizationRunId = optimizationRunId;
    }

    public SolverStatus getStatus() {
        return status;
    }

    public void setStatus(SolverStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public ScoreDto getScore() {
        return score;
    }

    public void setScore(ScoreDto score) {
        this.score = score;
    }

    public OptimizationMetricsDto getMetrics() {
        return metrics;
    }

    public void setMetrics(OptimizationMetricsDto metrics) {
        this.metrics = metrics;
    }

    public List<RouteResultDto> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteResultDto> routes) {
        this.routes = routes;
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

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
