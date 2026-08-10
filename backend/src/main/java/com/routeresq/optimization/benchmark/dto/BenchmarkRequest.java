package com.routeresq.optimization.benchmark.dto;

import com.routeresq.optimization.benchmark.model.BenchmarkDataset;
import jakarta.validation.constraints.NotNull;

public class BenchmarkRequest {

    @NotNull(message = "Dataset is required")
    private BenchmarkDataset dataset = BenchmarkDataset.MEDIUM;

    private Integer maxSolveSeconds = 5;

    public BenchmarkRequest() {
    }

    public BenchmarkRequest(BenchmarkDataset dataset, Integer maxSolveSeconds) {
        this.dataset = dataset;
        this.maxSolveSeconds = maxSolveSeconds;
    }

    public BenchmarkDataset getDataset() {
        return dataset;
    }

    public void setDataset(BenchmarkDataset dataset) {
        this.dataset = dataset;
    }

    public Integer getMaxSolveSeconds() {
        return maxSolveSeconds;
    }

    public void setMaxSolveSeconds(Integer maxSolveSeconds) {
        this.maxSolveSeconds = maxSolveSeconds;
    }
}
