package com.routeresq.optimization;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.optimization.dto.OptimizationRunRequest;
import com.routeresq.optimization.dto.OptimizationRunResponse;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.model.SolverStatus;
import com.routeresq.optimization.repository.OptimizationRunRepository;
import com.routeresq.optimization.service.OptimizationService;
import com.routeresq.optimization.solver.engine.OptimizationEngine;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.routing.provider.HaversineRoutingProvider;
import com.routeresq.routing.repository.RouteRepository;
import com.routeresq.routing.repository.RouteStopRepository;
import com.routeresq.shared.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizationEngineUnitTest {

    @Mock
    private DepotRepository depotRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private OptimizationRunRepository optimizationRunRepository;

    private OptimizationService optimizationService;

    private Depot testDepot;
    private List<Order> testOrders;
    private List<Vehicle> testVehicles;

    @BeforeEach
    void setUp() {
        HaversineRoutingProvider routingProvider = new HaversineRoutingProvider();
        OptimizationEngine optimizationEngine = new OptimizationEngine(routingProvider);
        optimizationService = new OptimizationService(
                depotRepository,
                orderRepository,
                vehicleRepository,
                routeRepository,
                routeStopRepository,
                optimizationRunRepository,
                optimizationEngine
        );

        UUID depotId = UUID.randomUUID();
        testDepot = Depot.builder()
                .name("Chicago Main Hub")
                .location(GeometryUtils.createPoint(41.8781, -87.6298))
                .addressText("100 S Wacker Dr, Chicago, IL")
                .build();
        testDepot.setId(depotId);

        Order o1 = Order.builder().orderNumber("ORD-001").customerName("Alice").location(GeometryUtils.createPoint(41.8850, -87.6300)).weightKg(new BigDecimal("15.0")).windowStartMinutes(540).windowEndMinutes(720).status(OrderStatus.UNASSIGNED).build();
        o1.setId(UUID.randomUUID());
        Order o2 = Order.builder().orderNumber("ORD-002").customerName("Bob").location(GeometryUtils.createPoint(41.8900, -87.6350)).weightKg(new BigDecimal("25.0")).windowStartMinutes(540).windowEndMinutes(720).status(OrderStatus.UNASSIGNED).build();
        o2.setId(UUID.randomUUID());
        Order o3 = Order.builder().orderNumber("ORD-003").customerName("Charlie").location(GeometryUtils.createPoint(41.8750, -87.6400)).weightKg(new BigDecimal("30.0")).windowStartMinutes(600).windowEndMinutes(780).status(OrderStatus.UNASSIGNED).build();
        o3.setId(UUID.randomUUID());
        Order o4 = Order.builder().orderNumber("ORD-004").customerName("Diana").location(GeometryUtils.createPoint(41.8600, -87.6200)).weightKg(new BigDecimal("10.0")).windowStartMinutes(600).windowEndMinutes(780).status(OrderStatus.UNASSIGNED).build();
        o4.setId(UUID.randomUUID());
        Order o5 = Order.builder().orderNumber("ORD-005").customerName("Eve").location(GeometryUtils.createPoint(41.8950, -87.6100)).weightKg(new BigDecimal("20.0")).windowStartMinutes(660).windowEndMinutes(840).status(OrderStatus.UNASSIGNED).build();
        o5.setId(UUID.randomUUID());

        testOrders = List.of(o1, o2, o3, o4, o5);

        Vehicle v1 = Vehicle.builder().vehicleCode("V-101").maxWeightKg(new BigDecimal("200.0")).currentLocation(GeometryUtils.createPoint(41.8781, -87.6298)).build();
        v1.setId(UUID.randomUUID());
        Vehicle v2 = Vehicle.builder().vehicleCode("V-102").maxWeightKg(new BigDecimal("200.0")).currentLocation(GeometryUtils.createPoint(41.8781, -87.6298)).build();
        v2.setId(UUID.randomUUID());

        testVehicles = List.of(v1, v2);
    }

    @Test
    @DisplayName("Unit Test: Solve Small Problem (5 Orders, 2 Vehicles)")
    void testSolveSmallProblem() {
        when(depotRepository.findById(testDepot.getId())).thenReturn(Optional.of(testDepot));
        when(orderRepository.findByDepotIdAndStatus(testDepot.getId(), OrderStatus.UNASSIGNED)).thenReturn(testOrders);
        when(vehicleRepository.findByDepotId(testDepot.getId())).thenReturn(testVehicles);
        when(optimizationRunRepository.save(any(OptimizationRun.class))).thenAnswer(invocation -> {
            OptimizationRun run = invocation.getArgument(0);
            if (run.getId() == null) run.setId(UUID.randomUUID());
            return run;
        });

        OptimizationRunRequest request = new OptimizationRunRequest(testDepot.getId(), null, null, 2);
        OptimizationRunResponse response = optimizationService.runOptimization(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SolverStatus.FEASIBLE);
        assertThat(response.getMetrics().getOrdersAssigned()).isEqualTo(5);
        assertThat(response.getMetrics().getUnassignedOrders()).isEqualTo(0);
        assertThat(response.getScore().getHard()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Unit Test: Detect Capacity Infeasibility")
    void testDetectCapacityInfeasibility() {
        when(depotRepository.findById(testDepot.getId())).thenReturn(Optional.of(testDepot));
        when(orderRepository.findByDepotIdAndStatus(testDepot.getId(), OrderStatus.UNASSIGNED)).thenReturn(testOrders);

        // Small capacity vehicle (20kg capacity vs 100kg total order demand)
        Vehicle vTiny = Vehicle.builder().vehicleCode("V-TINY").maxWeightKg(new BigDecimal("20.0")).build();
        vTiny.setId(UUID.randomUUID());
        List<Vehicle> tinyVehicles = List.of(vTiny);

        when(vehicleRepository.findByDepotId(testDepot.getId())).thenReturn(tinyVehicles);
        when(optimizationRunRepository.save(any(OptimizationRun.class))).thenAnswer(invocation -> {
            OptimizationRun run = invocation.getArgument(0);
            if (run.getId() == null) run.setId(UUID.randomUUID());
            return run;
        });

        OptimizationRunRequest request = new OptimizationRunRequest(testDepot.getId(), null, null, 2);
        OptimizationRunResponse response = optimizationService.runOptimization(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SolverStatus.FAILED);
        assertThat(response.getFailureReason()).contains("exceeds total fleet capacity");
    }
}
