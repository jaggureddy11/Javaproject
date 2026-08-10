package com.routeresq.optimization.benchmark.dto;

import com.routeresq.optimization.benchmark.model.BenchmarkDataset;

import java.time.Instant;

public class BenchmarkResult {

    private BenchmarkDataset dataset;
    private Instant timestamp;
    private int ordersCount;
    private int vehiclesCount;
    private BenchmarkMetrics baseline;
    private BenchmarkMetrics optimized;
    private ImprovementMetrics improvement;

    public BenchmarkResult() {
    }

    public BenchmarkResult(BenchmarkDataset dataset, Instant timestamp, int ordersCount, int vehiclesCount, BenchmarkMetrics baseline, BenchmarkMetrics optimized, ImprovementMetrics improvement) {
        this.dataset = dataset;
        this.timestamp = timestamp;
        this.ordersCount = ordersCount;
        this.vehiclesCount = vehiclesCount;
        this.baseline = baseline;
        this.optimized = optimized;
        this.improvement = improvement;
    }

    public BenchmarkDataset getDataset() {
        return dataset;
    }

    public void setDataset(BenchmarkDataset dataset) {
        this.dataset = dataset;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }

    public int getVehiclesCount() {
        return vehiclesCount;
    }

    public void setVehiclesCount(int vehiclesCount) {
        this.vehiclesCount = vehiclesCount;
    }

    public BenchmarkMetrics getBaseline() {
        return baseline;
    }

    public void setBaseline(BenchmarkMetrics baseline) {
        this.baseline = baseline;
    }

    public BenchmarkMetrics getOptimized() {
        return optimized;
    }

    public void setOptimized(BenchmarkMetrics optimized) {
        this.optimized = optimized;
    }

    public ImprovementMetrics getImprovement() {
        return improvement;
    }

    public void setImprovement(ImprovementMetrics improvement) {
        this.improvement = improvement;
    }
}
