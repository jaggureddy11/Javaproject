package com.routeresq.optimization.benchmark.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ImprovementMetrics {

    private double distanceImprovementPercent;
    private double durationImprovementPercent;
    private double vehicleReductionPercent;

    public ImprovementMetrics() {
    }

    public ImprovementMetrics(double distanceImprovementPercent, double durationImprovementPercent, double vehicleReductionPercent) {
        this.distanceImprovementPercent = distanceImprovementPercent;
        this.durationImprovementPercent = durationImprovementPercent;
        this.vehicleReductionPercent = vehicleReductionPercent;
    }

    public static ImprovementMetrics calculate(BenchmarkMetrics baseline, BenchmarkMetrics optimized) {
        double distImp = calculatePercent(baseline.getDistanceKm(), optimized.getDistanceKm());
        double durImp = calculatePercent(baseline.getDurationMinutes(), optimized.getDurationMinutes());
        double vehImp = calculatePercent(baseline.getVehiclesUsed(), optimized.getVehiclesUsed());

        return new ImprovementMetrics(distImp, durImp, vehImp);
    }

    private static double calculatePercent(double base, double opt) {
        if (base <= 0.0001) {
            return 0.0;
        }
        double val = ((base - opt) / base) * 100.0;
        return new BigDecimal(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public double getDistanceImprovementPercent() {
        return distanceImprovementPercent;
    }

    public void setDistanceImprovementPercent(double distanceImprovementPercent) {
        this.distanceImprovementPercent = distanceImprovementPercent;
    }

    public double getDurationImprovementPercent() {
        return durationImprovementPercent;
    }

    public void setDurationImprovementPercent(double durationImprovementPercent) {
        this.durationImprovementPercent = durationImprovementPercent;
    }

    public double getVehicleReductionPercent() {
        return vehicleReductionPercent;
    }

    public void setVehicleReductionPercent(double vehicleReductionPercent) {
        this.vehicleReductionPercent = vehicleReductionPercent;
    }
}
