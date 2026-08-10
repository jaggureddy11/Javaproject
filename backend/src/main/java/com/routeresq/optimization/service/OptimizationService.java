package com.routeresq.optimization.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.optimization.dto.OptimizationMetricsDto;
import com.routeresq.optimization.dto.OptimizationRunRequest;
import com.routeresq.optimization.dto.OptimizationRunResponse;
import com.routeresq.optimization.dto.RouteResultDto;
import com.routeresq.optimization.dto.ScoreDto;
import com.routeresq.optimization.dto.StopResultDto;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.model.OptimizationRunType;
import com.routeresq.optimization.model.SolverStatus;
import com.routeresq.optimization.repository.OptimizationRunRepository;
import com.routeresq.optimization.solver.engine.OptimizationEngine;
import com.routeresq.optimization.solver.model.RoutePlanSolution;
import com.routeresq.optimization.solver.model.Standstill;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.routing.matrix.DistanceMatrix;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStatus;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.routing.provider.RoutingProvider;
import com.routeresq.routing.repository.RouteRepository;
import com.routeresq.routing.repository.RouteStopRepository;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.shared.util.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OptimizationService {

    private static final Logger log = LoggerFactory.getLogger(OptimizationService.class);

    private final DepotRepository depotRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final OptimizationRunRepository optimizationRunRepository;
    private final OptimizationEngine optimizationEngine;

    public OptimizationService(DepotRepository depotRepository,
                               OrderRepository orderRepository,
                               VehicleRepository vehicleRepository,
                               RouteRepository routeRepository,
                               RouteStopRepository routeStopRepository,
                               OptimizationRunRepository optimizationRunRepository,
                               OptimizationEngine optimizationEngine) {
        this.depotRepository = depotRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.optimizationRunRepository = optimizationRunRepository;
        this.optimizationEngine = optimizationEngine;
    }

    @Transactional
    public OptimizationRunResponse runOptimization(OptimizationRunRequest request) {
        Instant startTime = Instant.now();

        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot", request.getDepotId()));

        // 1. Fetch Orders
        List<Order> orders;
        if (request.getOrderIds() != null && !request.getOrderIds().isEmpty()) {
            orders = orderRepository.findAllById(request.getOrderIds());
        } else {
            orders = orderRepository.findByDepotIdAndStatus(depot.getId(), OrderStatus.UNASSIGNED);
        }

        // 2. Fetch Vehicles
        List<Vehicle> vehicles;
        if (request.getVehicleIds() != null && !request.getVehicleIds().isEmpty()) {
            vehicles = vehicleRepository.findAllById(request.getVehicleIds());
        } else {
            vehicles = vehicleRepository.findByDepotId(depot.getId());
        }

        // Initialize OptimizationRun record
        OptimizationRun run = optimizationRunRepository.save(OptimizationRun.builder()
                .runType(OptimizationRunType.INITIAL)
                .solverStatus(SolverStatus.SOLVING)
                .build());

        // Check early infeasibility
        if (vehicles.isEmpty()) {
            return handleInfeasibleRun(run, "No available vehicles at depot " + depot.getName(), startTime, orders.size());
        }
        if (orders.isEmpty()) {
            return handleEmptyRun(run, startTime);
        }

        double totalWeight = orders.stream().mapToDouble(o -> o.getWeightKg().doubleValue()).sum();
        double totalCapacity = vehicles.stream().mapToDouble(v -> v.getMaxWeightKg().doubleValue()).sum();

        if (totalWeight > totalCapacity) {
            String msg = String.format("Total order demand (%.2f kg) exceeds total fleet capacity (%.2f kg)", totalWeight, totalCapacity);
            return handleInfeasibleRun(run, msg, startTime, orders.size());
        }

        // 3. Solve via OptimizationEngine
        int solveSeconds = request.getMaxSolveSeconds() != null ? request.getMaxSolveSeconds() : 10;
        RoutePlanSolution bestSolution = optimizationEngine.solve(depot, vehicles, orders, solveSeconds);

        Instant completedTime = Instant.now();
        long durationMs = Duration.between(startTime, completedTime).toMillis();

        HardSoftScore score = bestSolution.getScore();
        int hardScore = score != null ? score.hardScore() : 0;
        int softScore = score != null ? score.softScore() : 0;

        if (hardScore < 0) {
            return handleInfeasibleRun(run, "Solver returned infeasible score: " + hardScore + " hard", startTime, orders.size());
        }

        RoutingProvider routingProvider = optimizationEngine.getRoutingProvider();
        DistanceMatrix distanceMatrix = bestSolution.getDistanceMatrix();

        // Reconstruct next customer map from previousStandstill links
        Map<Standstill, TimefoldCustomer> nextMap = new HashMap<>();
        for (TimefoldCustomer c : bestSolution.getCustomerList()) {
            if (c.getPreviousStandstill() != null) {
                nextMap.put(c.getPreviousStandstill(), c);
            }
        }

        Map<UUID, Order> orderMap = orders.stream().collect(Collectors.toMap(Order::getId, o -> o));
        Map<UUID, Vehicle> vehicleMap = vehicles.stream().collect(Collectors.toMap(Vehicle::getId, v -> v));

        // 4. Persist Feasible Solution
        List<RouteResultDto> routeResults = new ArrayList<>();
        int vehiclesUsed = 0;
        int ordersAssigned = 0;
        double totalDistanceMeters = 0.0;
        int totalDurationMinutes = 0;

        for (TimefoldVehicle tfVehicle : bestSolution.getVehicleList()) {
            TimefoldCustomer current = nextMap.get(tfVehicle);
            if (current == null) {
                continue; // Unused vehicle
            }

            vehiclesUsed++;
            Vehicle vehicle = vehicleMap.get(tfVehicle.getId());

            Route savedRoute = routeRepository.save(Route.builder()
                    .optimizationRun(run)
                    .vehicle(vehicle)
                    .versionNumber(1)
                    .status(RouteStatus.PLANNED)
                    .build());
            UUID routeId = savedRoute != null ? savedRoute.getId() : UUID.randomUUID();

            List<StopResultDto> stopResults = new ArrayList<>();
            int sequence = 1;
            Standstill prev = tfVehicle;
            double routeDistanceMeters = 0.0;
            int routeDurationMinutes = 0;

            while (current != null) {
                ordersAssigned++;
                Order order = orderMap.get(current.getId());
                order.setStatus(OrderStatus.ASSIGNED);
                orderRepository.save(order);

                double legDist = GeometryUtils.haversineMeters(prev.getLocation(), current.getLocation());
                int legTime = routingProvider.getTravelTimeMinutes(prev.getLocation(), current.getLocation());

                routeDistanceMeters += legDist;
                routeDurationMinutes += legTime;

                Integer arrivalVal = current.getArrivalTimeMinutes(distanceMatrix);
                int arrival = arrivalVal != null ? arrivalVal : 540;
                int departure = arrival + current.getServiceDurationMinutes();

                RouteStop savedStop = routeStopRepository.save(RouteStop.builder()
                        .route(savedRoute)
                        .order(order)
                        .sequenceNumber(sequence)
                        .estimatedArrivalMinutes(arrival)
                        .estimatedDepartureMinutes(departure)
                        .stopStatus(StopStatus.PENDING)
                        .build());
                UUID stopId = savedStop != null ? savedStop.getId() : UUID.randomUUID();

                stopResults.add(new StopResultDto(
                        stopId,
                        order.getId(),
                        order.getOrderNumber(),
                        order.getCustomerName(),
                        sequence,
                        arrival,
                        departure
                ));

                sequence++;
                prev = current;
                current = nextMap.get(current);
            }

            // Return to depot leg
            routeDistanceMeters += GeometryUtils.haversineMeters(prev.getLocation(), depot.getLocation());
            routeDurationMinutes += routingProvider.getTravelTimeMinutes(prev.getLocation(), depot.getLocation());

            totalDistanceMeters += routeDistanceMeters;
            totalDurationMinutes += routeDurationMinutes;

            BigDecimal routeDistanceKm = new BigDecimal(routeDistanceMeters / 1000.0).setScale(2, RoundingMode.HALF_UP);
            if (savedRoute != null) {
                savedRoute.setTotalDistanceKm(routeDistanceKm);
                savedRoute.setTotalDurationMinutes(routeDurationMinutes);
                routeRepository.save(savedRoute);
            }

            routeResults.add(new RouteResultDto(
                    routeId,
                    vehicle.getId(),
                    vehicle.getVehicleCode(),
                    vehicle.getDriver() != null ? vehicle.getDriver().getId() : null,
                    vehicle.getDriver() != null ? vehicle.getDriver().getName() : null,
                    RouteStatus.PLANNED,
                    (int) Math.round(routeDistanceMeters),
                    routeDurationMinutes,
                    stopResults
            ));
        }

        // Update OptimizationRun
        run.setSolverStatus(SolverStatus.FEASIBLE);
        run.setHardScore(hardScore);
        run.setSoftScore(softScore);
        run.setExecutionDurationMs((int) durationMs);
        run.setTotalDistanceKm(new BigDecimal(totalDistanceMeters / 1000.0).setScale(2, RoundingMode.HALF_UP));
        run.setTotalDurationMinutes(totalDurationMinutes);
        optimizationRunRepository.save(run);

        double totalDistanceKm = totalDistanceMeters / 1000.0;
        int unassignedCount = orders.size() - ordersAssigned;

        OptimizationMetricsDto metrics = new OptimizationMetricsDto(
                Math.round(totalDistanceKm * 100.0) / 100.0,
                totalDurationMinutes,
                vehiclesUsed,
                ordersAssigned,
                unassignedCount
        );

        return new OptimizationRunResponse(
                run.getId(),
                SolverStatus.FEASIBLE,
                null,
                new ScoreDto(hardScore, softScore),
                metrics,
                routeResults,
                startTime,
                completedTime,
                durationMs
        );
    }

    @Transactional(readOnly = true)
    public OptimizationRunResponse getRun(UUID id) {
        OptimizationRun run = optimizationRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OptimizationRun", id));

        return new OptimizationRunResponse(
                run.getId(),
                run.getSolverStatus(),
                null,
                new ScoreDto(run.getHardScore() != null ? run.getHardScore() : 0, run.getSoftScore() != null ? run.getSoftScore() : 0),
                new OptimizationMetricsDto(run.getTotalDistanceKm() != null ? run.getTotalDistanceKm().doubleValue() : 0.0, run.getTotalDurationMinutes() != null ? run.getTotalDurationMinutes() : 0, 0, 0, 0),
                List.of(),
                run.getCreatedAt(),
                run.getUpdatedAt(),
                run.getExecutionDurationMs() != null ? run.getExecutionDurationMs().longValue() : 0L
        );
    }

    private OptimizationRunResponse handleInfeasibleRun(OptimizationRun run, String reason, Instant startTime, int ordersCount) {
        Instant completedTime = Instant.now();
        long durationMs = Duration.between(startTime, completedTime).toMillis();

        run.setSolverStatus(SolverStatus.FAILED);
        run.setExecutionDurationMs((int) durationMs);
        optimizationRunRepository.save(run);

        return new OptimizationRunResponse(
                run.getId(),
                SolverStatus.FAILED,
                reason,
                new ScoreDto(-9999, -9999),
                new OptimizationMetricsDto(0.0, 0, 0, 0, ordersCount),
                List.of(),
                startTime,
                completedTime,
                durationMs
        );
    }

    private OptimizationRunResponse handleEmptyRun(OptimizationRun run, Instant startTime) {
        Instant completedTime = Instant.now();
        long durationMs = Duration.between(startTime, completedTime).toMillis();

        run.setSolverStatus(SolverStatus.FEASIBLE);
        run.setHardScore(0);
        run.setSoftScore(0);
        run.setExecutionDurationMs((int) durationMs);
        optimizationRunRepository.save(run);

        return new OptimizationRunResponse(
                run.getId(),
                SolverStatus.FEASIBLE,
                null,
                new ScoreDto(0, 0),
                new OptimizationMetricsDto(0.0, 0, 0, 0, 0),
                List.of(),
                startTime,
                completedTime,
                durationMs
        );
    }
}
