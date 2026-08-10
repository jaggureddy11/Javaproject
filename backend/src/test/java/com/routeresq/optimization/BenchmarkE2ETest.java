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

class BenchmarkE2ETest {

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
    @DisplayName("E2E Benchmark: SMALL Dataset (5 Orders / 2 Vehicles)")
    void testSmallDatasetBenchmark() {
        BenchmarkRequest request = new BenchmarkRequest(BenchmarkDataset.SMALL, 2);
        BenchmarkResult result = benchmarkService.runBenchmark(request);

        assertThat(result).isNotNull();
        assertThat(result.getOrdersCount()).isEqualTo(5);
        assertThat(result.getVehiclesCount()).isEqualTo(2);
        assertThat(result.getBaseline().getOrdersAssigned()).isEqualTo(5);
        assertThat(result.getOptimized().getOrdersAssigned()).isEqualTo(5);

        System.out.printf("BENCHMARK RESULT [SMALL]: Baseline Distance = %.2f km, Optimized Distance = %.2f km (Improvement: %.1f%%)\n",
                result.getBaseline().getDistanceKm(),
                result.getOptimized().getDistanceKm(),
                result.getImprovement().getDistanceImprovementPercent());
    }

    @Test
    @DisplayName("E2E Benchmark Suite Execution Across All 6 Datasets")
    void testAllDatasetsBenchmarkSuite() {
        System.out.println("==========================================================================================================================");
        System.out.println("                                          ROUTERESQ EMPIRICAL BENCHMARK SUITE RESULTS                                      ");
        System.out.println("==========================================================================================================================");
        System.out.printf("| %-18s | %-6s | %-8s | %-12s | %-12s | %-11s | %-12s | %-12s | %-14s |\n",
                "Dataset", "Orders", "Vehicles", "Base Dist KM", "Opt Dist KM", "Dist Imp %", "Base Duration", "Opt Duration", "Solve Time (ms)");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        for (BenchmarkDataset ds : BenchmarkDataset.values()) {
            int solveSec = (ds == BenchmarkDataset.LARGE) ? 5 : 2;
            BenchmarkRequest req = new BenchmarkRequest(ds, solveSec);
            BenchmarkResult res = benchmarkService.runBenchmark(req);

            assertThat(res).isNotNull();
            assertThat(res.getBaseline()).isNotNull();
            assertThat(res.getOptimized()).isNotNull();

            System.out.printf("| %-18s | %-6d | %-8d | %-12.2f | %-12.2f | %-11.1f%% | %-12d | %-12d | %-14d |\n",
                    res.getDataset().name(),
                    res.getOrdersCount(),
                    res.getVehiclesCount(),
                    res.getBaseline().getDistanceKm(),
                    res.getOptimized().getDistanceKm(),
                    res.getImprovement().getDistanceImprovementPercent(),
                    res.getBaseline().getDurationMinutes(),
                    res.getOptimized().getDurationMinutes(),
                    res.getOptimized().getSolveTimeMs());
        }
        System.out.println("==========================================================================================================================");
    }
}
