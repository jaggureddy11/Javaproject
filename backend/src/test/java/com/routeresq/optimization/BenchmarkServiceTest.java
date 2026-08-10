package com.routeresq.optimization;

import com.routeresq.optimization.benchmark.dto.BenchmarkMetrics;
import com.routeresq.optimization.benchmark.dto.ImprovementMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BenchmarkServiceTest {

    @Test
    @DisplayName("ImprovementMetrics calculates percentage reductions correctly")
    void testImprovementCalculation() {
        BenchmarkMetrics baseline = new BenchmarkMetrics(100.0, 200, 150, 50, 5, 5, 25, 0, 3, 0, 0, false, 10);
        BenchmarkMetrics optimized = new BenchmarkMetrics(75.0, 160, 110, 50, 4, 4, 25, 0, 0, 0, 0, true, 2000);

        ImprovementMetrics improvement = ImprovementMetrics.calculate(baseline, optimized);

        assertThat(improvement.getDistanceImprovementPercent()).isEqualTo(25.0); // (100 - 75) / 100 = 25.0%
        assertThat(improvement.getDurationImprovementPercent()).isEqualTo(20.0); // (200 - 160) / 200 = 20.0%
        assertThat(improvement.getVehicleReductionPercent()).isEqualTo(20.0);     // (5 - 4) / 5 = 20.0%
    }

    @Test
    @DisplayName("ImprovementMetrics handles zero baseline distance safely without divide-by-zero exception")
    void testZeroBaselineSafeHandling() {
        BenchmarkMetrics baseline = new BenchmarkMetrics(0.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 5);
        BenchmarkMetrics optimized = new BenchmarkMetrics(0.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 100);

        ImprovementMetrics improvement = ImprovementMetrics.calculate(baseline, optimized);

        assertThat(improvement.getDistanceImprovementPercent()).isEqualTo(0.0);
        assertThat(improvement.getDurationImprovementPercent()).isEqualTo(0.0);
        assertThat(improvement.getVehicleReductionPercent()).isEqualTo(0.0);
    }
}
