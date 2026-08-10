package com.routeresq.optimization;

import com.routeresq.optimization.baseline.service.BaselineRoutePlanner;
import com.routeresq.optimization.benchmark.dto.BenchmarkRequest;
import com.routeresq.optimization.benchmark.dto.BenchmarkResult;
import com.routeresq.optimization.benchmark.model.BenchmarkDataset;
import com.routeresq.optimization.benchmark.service.BenchmarkDataGenerator;
import com.routeresq.optimization.benchmark.service.BenchmarkService;
import com.routeresq.optimization.solver.engine.OptimizationEngine;
import com.routeresq.routing.provider.HaversineRoutingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticRunnerTest {

    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        HaversineRoutingProvider routingProvider = new HaversineRoutingProvider();
        OptimizationEngine optimizationEngine = new OptimizationEngine(routingProvider);
        benchmarkService = new BenchmarkService(
                new BaselineRoutePlanner(),
                new BenchmarkDataGenerator(),
                optimizationEngine
        );
    }

    @Test
    @DisplayName("DIAGNOSTIC 1: Benchmark Quality Review for All 6 Datasets")
    void testAllDatasetsQualityReview() {
        System.out.println("==========================================================================================================================");
        System.out.println("                                    ROUTERESQ CHECKPOINT 6 FINAL BENCHMARK REVIEW                                         ");
        System.out.println("==========================================================================================================================");
        System.out.printf("| %-18s | %-6s | %-8s | %-12s | %-12s | %-10s | %-8s | %-8s | %-8s |\n",
                "Dataset", "Orders", "Vehicles", "Base Dist KM", "Opt Dist KM", "Dist Imp %", "Base Late", "Opt Late", "Feasible");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        for (BenchmarkDataset ds : BenchmarkDataset.values()) {
            int solveSec = (ds == BenchmarkDataset.LARGE) ? 5 : 2;
            BenchmarkRequest req = new BenchmarkRequest(ds, solveSec);
            BenchmarkResult res = benchmarkService.runBenchmark(req);

            assertThat(res).isNotNull();

            System.out.printf("| %-18s | %-6d | %-8d | %-12.2f | %-12.2f | %-10.1f%% | %-8d | %-8d | %-8b |\n",
                    res.getDataset().name(),
                    res.getOrdersCount(),
                    res.getVehiclesCount(),
                    res.getBaseline().getDistanceKm(),
                    res.getOptimized().getDistanceKm(),
                    res.getImprovement().getDistanceImprovementPercent(),
                    res.getBaseline().getLateDeliveries(),
                    res.getOptimized().getLateDeliveries(),
                    res.getOptimized().isFeasible());
        }
        System.out.println("==========================================================================================================================");
    }

    @Test
    @DisplayName("DIAGNOSTIC 2: Solver Scaling Analysis on LARGE Dataset Across Time Limits (1s, 2s, 5s, 10s, 20s, 30s)")
    void testLargeDatasetScalingAnalysis() {
        System.out.println("\n==========================================================================================================================");
        System.out.println("                                DIAGNOSTIC: LARGE DATASET SOLVER CONVERGENCE & SCALING                                    ");
        System.out.println("==========================================================================================================================");
        System.out.printf("| %-10s | %-12s | %-12s | %-10s | %-10s | %-10s | %-8s |\n",
                "Time Limit", "Base Dist KM", "Opt Dist KM", "Dist Imp %", "Base Late", "Opt Late", "Feasible");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        int[] timeLimits = {1, 2, 5, 10, 20, 30};
        for (int sec : timeLimits) {
            BenchmarkRequest req = new BenchmarkRequest(BenchmarkDataset.LARGE, sec);
            BenchmarkResult res = benchmarkService.runBenchmark(req);

            System.out.printf("| %-10s | %-12.2f | %-12.2f | %-10.1f%% | %-10d | %-10d | %-8b |\n",
                    sec + "s",
                    res.getBaseline().getDistanceKm(),
                    res.getOptimized().getDistanceKm(),
                    res.getImprovement().getDistanceImprovementPercent(),
                    res.getBaseline().getLateDeliveries(),
                    res.getOptimized().getLateDeliveries(),
                    res.getOptimized().isFeasible());
        }
        System.out.println("==========================================================================================================================");
    }
}
