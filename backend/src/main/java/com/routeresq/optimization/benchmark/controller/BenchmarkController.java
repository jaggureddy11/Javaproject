package com.routeresq.optimization.benchmark.controller;

import com.routeresq.optimization.benchmark.dto.BenchmarkRequest;
import com.routeresq.optimization.benchmark.dto.BenchmarkResult;
import com.routeresq.optimization.benchmark.service.BenchmarkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/optimization/benchmarks")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping
    public ResponseEntity<BenchmarkResult> runBenchmark(@Valid @RequestBody BenchmarkRequest request) {
        BenchmarkResult result = benchmarkService.runBenchmark(request);
        return ResponseEntity.ok(result);
    }
}
