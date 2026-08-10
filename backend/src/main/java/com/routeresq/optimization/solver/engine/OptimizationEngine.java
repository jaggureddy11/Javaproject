package com.routeresq.optimization.solver.engine;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import ai.timefold.solver.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.optimization.solver.constraint.VRPTWConstraintProvider;
import com.routeresq.optimization.solver.model.RoutePlanSolution;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.order.model.Order;
import com.routeresq.routing.matrix.DistanceMatrix;
import com.routeresq.routing.provider.RoutingProvider;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OptimizationEngine {

    private static final Logger log = LoggerFactory.getLogger(OptimizationEngine.class);

    private final RoutingProvider routingProvider;

    public OptimizationEngine(RoutingProvider routingProvider) {
        this.routingProvider = routingProvider;
    }

    public RoutePlanSolution solve(Depot depot, List<Vehicle> vehicles, List<Order> orders, int maxSolveSeconds) {
        log.info("Building VRP problem for depot {} with {} orders and {} vehicles...", depot.getName(), orders.size(), vehicles.size());

        List<Point> locations = new ArrayList<>();
        locations.add(depot.getLocation());
        orders.forEach(o -> locations.add(o.getLocation()));
        vehicles.forEach(v -> {
            if (v.getCurrentLocation() != null) locations.add(v.getCurrentLocation());
        });

        DistanceMatrix distanceMatrix = DistanceMatrix.build(locations, routingProvider);

        List<TimefoldVehicle> timefoldVehicles = new ArrayList<>();
        for (Vehicle v : vehicles) {
            int shiftStart = v.getDriver() != null ? v.getDriver().getShiftStartMinutes() : 480;
            int shiftEnd = v.getDriver() != null ? v.getDriver().getShiftEndMinutes() : 1020;
            UUID driverId = v.getDriver() != null ? v.getDriver().getId() : null;
            String driverName = v.getDriver() != null ? v.getDriver().getName() : null;

            timefoldVehicles.add(new TimefoldVehicle(
                    v.getId(),
                    v.getVehicleCode(),
                    depot.getId(),
                    depot.getLocation(),
                    v.getMaxWeightKg(),
                    shiftStart,
                    shiftEnd,
                    driverId,
                    driverName
            ));
        }

        List<TimefoldCustomer> timefoldCustomers = new ArrayList<>();
        for (Order o : orders) {
            timefoldCustomers.add(new TimefoldCustomer(
                    o.getId(),
                    o.getOrderNumber(),
                    o.getCustomerName(),
                    o.getLocation(),
                    o.getWeightKg(),
                    o.getWindowStartMinutes(),
                    o.getWindowEndMinutes(),
                    o.getServiceDurationMinutes(),
                    o.getPriority()
            ));
        }

        RoutePlanSolution problem = new RoutePlanSolution(timefoldVehicles, timefoldCustomers, distanceMatrix);

        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig()
                .withAcceptorConfig(new LocalSearchAcceptorConfig().withLateAcceptanceSize(400))
                .withForagerConfig(new LocalSearchForagerConfig().withAcceptedCountLimit(1))
                .withMoveSelectorConfig(new UnionMoveSelectorConfig(List.of(
                        new ChangeMoveSelectorConfig(),
                        new SwapMoveSelectorConfig(),
                        new SubChainChangeMoveSelectorConfig(),
                        new SubChainSwapMoveSelectorConfig()
                )));

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(RoutePlanSolution.class)
                .withEntityClassList(List.of(TimefoldCustomer.class))
                .withConstraintProviderClass(VRPTWConstraintProvider.class)
                .withPhaseList(List.of(
                        new ConstructionHeuristicPhaseConfig()
                                .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING),
                        localSearchPhaseConfig
                ))
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit((long) maxSolveSeconds));

        SolverFactory<RoutePlanSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<RoutePlanSolution> solver = solverFactory.buildSolver();

        return solver.solve(problem);
    }

    public RoutingProvider getRoutingProvider() {
        return routingProvider;
    }
}
