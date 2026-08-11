package com.routeresq.optimization.controller;

import com.routeresq.optimization.dto.OptimizationRunRequest;
import com.routeresq.optimization.dto.OptimizationRunResponse;
import com.routeresq.optimization.service.OptimizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    /**
     * POST /api/v1/optimization/runs
     *
     * Returns 202 Accepted immediately with a run ID (status = SOLVING).
     * The client should either:
     *   a) Subscribe to WebSocket topic /topic/optimization/{runId}, or
     *   b) Poll GET /api/v1/optimization/runs/{id} every 2 seconds.
     */
    @PostMapping("/runs")
    public ResponseEntity<OptimizationRunResponse> startOptimization(
            @Valid @RequestBody OptimizationRunRequest request) {
        OptimizationRunResponse response = optimizationService.startOptimization(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<OptimizationRunResponse> getRun(@PathVariable UUID id) {
        OptimizationRunResponse response = optimizationService.getRun(id);
        return ResponseEntity.ok(response);
    }
}
