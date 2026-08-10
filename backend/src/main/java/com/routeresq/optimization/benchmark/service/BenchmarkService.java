package com.routeresq.optimization.benchmark.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.optimization.baseline.model.BaselineRouteResult;
import com.routeresq.optimization.baseline.service.BaselineRoutePlanner;
import com.routeresq.optimization.benchmark.dto.BenchmarkMetrics;
import com.routeresq.optimization.benchmark.dto.BenchmarkRequest;
import com.routeresq.optimization.benchmark.dto.BenchmarkResult;
import com.routeresq.optimization.benchmark.dto.ImprovementMetrics;
import com.routeresq.optimization.solver.engine.OptimizationEngine;
import com.routeresq.optimization.solver.model.RoutePlanSolution;
import com.routeresq.optimization.solver.model.Standstill;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.order.model.Order;
import com.routeresq.routing.matrix.DistanceMatrix;
import com.routeresq.routing.provider.RoutingProvider;
import com.routeresq.shared.util.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class BenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);

    private final BaselineRoutePlanner baselineRoutePlanner;
    private final BenchmarkDataGenerator benchmarkDataGenerator;
    private final OptimizationEngine optimizationEngine;

    public BenchmarkService(BaselineRoutePlanner baselineRoutePlanner,
                            BenchmarkDataGenerator benchmarkDataGenerator,
                            OptimizationEngine optimizationEngine) {
        this.baselineRoutePlanner = baselineRoutePlanner;
        this.benchmarkDataGenerator = benchmarkDataGenerator;
        this.optimizationEngine = optimizationEngine;
    }

    public BenchmarkResult runBenchmark(BenchmarkRequest request) {
        log.info("Starting benchmark run for dataset {}...", request.getDataset());
        Instant timestamp = Instant.now();

        // 1. Generate SAME input data
        BenchmarkDataGenerator.BenchmarkDataHolder holder = benchmarkDataGenerator.generateData(request.getDataset());
        Depot depot = holder.getDepot();
        List<Vehicle> vehicles = holder.getVehicles();
        List<Order> orders = holder.getOrders();
        RoutingProvider routingProvider = optimizationEngine.getRoutingProvider();

        // 2. Execute Baseline Algorithm
        BaselineRouteResult baselineResult = baselineRoutePlanner.planRoutes(depot, vehicles, orders, routingProvider);

        BenchmarkMetrics baselineMetrics = new BenchmarkMetrics(
                baselineResult.getTotalDistanceKm(),
                baselineResult.getTotalDurationMinutes(),
                baselineResult.getTotalDurationMinutes(),
                orders.size() * 10,
                baselineResult.getVehiclesUsed(),
                baselineResult.getRoutes().size(),
                baselineResult.getOrdersAssigned(),
                baselineResult.getOrdersUnassigned(),
                baselineResult.getLateDeliveries(),
                baselineResult.getCapacityViolations(),
                baselineResult.getShiftViolations(),
                baselineResult.isFeasible(),
                baselineResult.getExecutionTimeMs()
        );

        // 3. Execute Timefold VRPTW Solver via OptimizationEngine
        long optStartMs = System.currentTimeMillis();
        int solveSeconds = request.getMaxSolveSeconds() != null ? request.getMaxSolveSeconds() : 5;

        RoutePlanSolution bestSolution = optimizationEngine.solve(depot, vehicles, orders, solveSeconds);

        long optEndMs = System.currentTimeMillis();
        long optSolveTimeMs = optEndMs - optStartMs;

        HardSoftScore score = bestSolution.getScore();
        boolean optFeasible = score != null && score.hardScore() >= 0;

        DistanceMatrix distanceMatrix = bestSolution.getDistanceMatrix();

        // Reconstruct next customer map
        Map<Standstill, TimefoldCustomer> nextMap = new HashMap<>();
        Set<UUID> optAssignedOrders = new HashSet<>();
        for (TimefoldCustomer c : bestSolution.getCustomerList()) {
            if (c.getPreviousStandstill() != null) {
                nextMap.put(c.getPreviousStandstill(), c);
            }
        }

        int optVehiclesUsed = 0;
        double optDistanceMeters = 0.0;
        int optDurationMinutes = 0;
        int optLateCount = 0;

        for (TimefoldVehicle tfVehicle : bestSolution.getVehicleList()) {
            TimefoldCustomer current = nextMap.get(tfVehicle);
            if (current == null) continue;

            optVehiclesUsed++;
            Standstill prev = tfVehicle;

            while (current != null) {
                optAssignedOrders.add(current.getId());
                optDistanceMeters += GeometryUtils.haversineMeters(prev.getLocation(), current.getLocation());
                optDurationMinutes += routingProvider.getTravelTimeMinutes(prev.getLocation(), current.getLocation());

                Integer arrival = current.getArrivalTimeMinutes(distanceMatrix);
                if (arrival != null && arrival > current.getWindowEndMinutes()) {
                    optLateCount++;
                }

                optDurationMinutes += current.getServiceDurationMinutes();
                prev = current;
                current = nextMap.get(current);
            }

            optDistanceMeters += GeometryUtils.haversineMeters(prev.getLocation(), depot.getLocation());
            optDurationMinutes += routingProvider.getTravelTimeMinutes(prev.getLocation(), depot.getLocation());
        }

        double optDistanceKm = new BigDecimal(optDistanceMeters / 1000.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
        int optUnassigned = orders.size() - optAssignedOrders.size();
        if (optUnassigned > 0) {
            optFeasible = false;
        }

        BenchmarkMetrics optimizedMetrics = new BenchmarkMetrics(
                optDistanceKm,
                optDurationMinutes,
                optDurationMinutes,
                optAssignedOrders.size() * 10,
                optVehiclesUsed,
                optVehiclesUsed,
                optAssignedOrders.size(),
                optUnassigned,
                optLateCount,
                0,
                0,
                optFeasible,
                optSolveTimeMs
        );

        // 4. Calculate Improvement
        ImprovementMetrics improvement = ImprovementMetrics.calculate(baselineMetrics, optimizedMetrics);

        return new BenchmarkResult(
                request.getDataset(),
                timestamp,
                orders.size(),
                vehicles.size(),
                baselineMetrics,
                optimizedMetrics,
                improvement
        );
    }
}
