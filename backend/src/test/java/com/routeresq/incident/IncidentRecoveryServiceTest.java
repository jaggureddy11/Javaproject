package com.routeresq.incident;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.incident.dto.ImpactAnalysisResult;
import com.routeresq.incident.dto.RecoveryPlanResponse;
import com.routeresq.incident.model.Incident;
import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;
import com.routeresq.incident.repository.IncidentRepository;
import com.routeresq.incident.service.IncidentImpactAnalyzer;
import com.routeresq.incident.service.IncidentRecoveryService;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.repository.OptimizationRunRepository;
import com.routeresq.optimization.solver.engine.OptimizationEngine;
import com.routeresq.optimization.solver.model.RoutePlanSolution;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStatus;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.routing.repository.RouteRepository;
import com.routeresq.routing.repository.RouteStopRepository;
import com.routeresq.shared.util.GeometryUtils;
import com.routeresq.simulation.service.SimulationService;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentRecoveryServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteStopRepository routeStopRepository;
    @Mock
    private OptimizationRunRepository optimizationRunRepository;
    @Mock
    private OptimizationEngine optimizationEngine;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private SimulationService simulationService;

    private IncidentImpactAnalyzer impactAnalyzer;
    private IncidentRecoveryService recoveryService;

    private Depot testDepot;
    private Vehicle brokenVehicle;
    private Vehicle replacementVehicle;
    private Route brokenRoute;
    private Order completedOrder;
    private Order affectedOrder;
    private Incident testIncident;

    @BeforeEach
    void setUp() {
        impactAnalyzer = new IncidentImpactAnalyzer(routeRepository, vehicleRepository);
        recoveryService = new IncidentRecoveryService(
                incidentRepository,
                impactAnalyzer,
                vehicleRepository,
                orderRepository,
                routeRepository,
                routeStopRepository,
                optimizationRunRepository,
                optimizationEngine,
                messagingTemplate,
                simulationService
        );

        testDepot = Depot.builder()
                .name("Chicago Main Hub")
                .location(GeometryUtils.createPoint(41.8781, -87.6298))
                .addressText("Wacker Dr")
                .build();
        testDepot.setId(UUID.randomUUID());

        brokenVehicle = Vehicle.builder()
                .vehicleCode("V-101")
                .depot(testDepot)
                .maxWeightKg(new BigDecimal("500"))
                .currentLocation(GeometryUtils.createPoint(41.8781, -87.6298))
                .build();
        brokenVehicle.setId(UUID.randomUUID());
        brokenVehicle.setStatus(VehicleStatus.EN_ROUTE);

        replacementVehicle = Vehicle.builder()
                .vehicleCode("V-102")
                .depot(testDepot)
                .maxWeightKg(new BigDecimal("500"))
                .currentLocation(GeometryUtils.createPoint(41.8781, -87.6298))
                .build();
        replacementVehicle.setId(UUID.randomUUID());
        replacementVehicle.setStatus(VehicleStatus.IDLE);

        brokenRoute = Route.builder()
                .vehicle(brokenVehicle)
                .versionNumber(1)
                .status(RouteStatus.ACTIVE)
                .build();
        brokenRoute.setId(UUID.randomUUID());

        completedOrder = Order.builder()
                .orderNumber("ORD-101")
                .customerName("Acme Corp")
                .location(GeometryUtils.createPoint(41.8850, -87.6300))
                .status(OrderStatus.DELIVERED)
                .build();
        completedOrder.setId(UUID.randomUUID());

        affectedOrder = Order.builder()
                .orderNumber("ORD-102")
                .customerName("Beta Inc")
                .location(GeometryUtils.createPoint(41.8900, -87.6400))
                .status(OrderStatus.ASSIGNED)
                .build();
        affectedOrder.setId(UUID.randomUUID());

        RouteStop stop1 = RouteStop.builder()
                .route(brokenRoute)
                .order(completedOrder)
                .sequenceNumber(1)
                .stopStatus(StopStatus.COMPLETED)
                .build();

        RouteStop stop2 = RouteStop.builder()
                .route(brokenRoute)
                .order(affectedOrder)
                .sequenceNumber(2)
                .stopStatus(StopStatus.PENDING)
                .build();

        brokenRoute.getStops().addAll(List.of(stop1, stop2));

        testIncident = Incident.builder()
                .incidentType(IncidentType.VEHICLE_BREAKDOWN)
                .status(IncidentStatus.OPEN)
                .vehicle(brokenVehicle)
                .description("Engine failure on I-90 West")
                .occurredAt(Instant.now())
                .build();
        testIncident.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Unit Test: Impact Analyzer Preserves Completed Deliveries")
    void testImpactAnalyzerPreservesCompletedStops() {
        when(routeRepository.findByVehicleId(brokenVehicle.getId())).thenReturn(List.of(brokenRoute));
        when(vehicleRepository.findAll()).thenReturn(List.of(brokenVehicle, replacementVehicle));

        ImpactAnalysisResult result = impactAnalyzer.analyzeImpact(testIncident);

        assertThat(result).isNotNull();
        assertThat(result.getCompletedStopsCount()).isEqualTo(1);
        assertThat(result.getAffectedOrdersCount()).isEqualTo(1);
        assertThat(result.getAffectedOrderIds()).containsExactly(affectedOrder.getId());
        assertThat(result.getCandidateVehicleCodes()).containsExactly("V-102");
    }

    @Test
    @DisplayName("Unit Test: Incident Recovery Execution & Route Version Increment")
    void testIncidentRecoveryExecution() {
        when(incidentRepository.findById(testIncident.getId())).thenReturn(Optional.of(testIncident));
        when(routeRepository.findByVehicleId(brokenVehicle.getId())).thenReturn(List.of(brokenRoute));
        when(vehicleRepository.findAll()).thenReturn(List.of(brokenVehicle, replacementVehicle));
        when(vehicleRepository.findAllById(any())).thenReturn(List.of(replacementVehicle));
        when(orderRepository.findAllById(any())).thenReturn(List.of(affectedOrder));
        when(routeRepository.findById(brokenRoute.getId())).thenReturn(Optional.of(brokenRoute));

        when(optimizationRunRepository.save(any(OptimizationRun.class))).thenAnswer(inv -> {
            OptimizationRun run = inv.getArgument(0);
            if (run.getId() == null) run.setId(UUID.randomUUID());
            return run;
        });

        // Mock Timefold solution
        TimefoldVehicle tfVeh = new TimefoldVehicle(replacementVehicle.getId(), replacementVehicle.getVehicleCode(), testDepot.getId(), testDepot.getLocation(), new BigDecimal("500"), 480, 1020, null, "John Doe");
        TimefoldCustomer tfCust = new TimefoldCustomer(affectedOrder.getId(), affectedOrder.getOrderNumber(), affectedOrder.getCustomerName(), affectedOrder.getLocation(), new BigDecimal("20"), 480, 600, 10, 1);
        tfVeh.setNextCustomer(tfCust);

        RoutePlanSolution solution = new RoutePlanSolution();
        solution.setVehicleList(List.of(tfVeh));
        solution.setCustomerList(List.of(tfCust));
        solution.setScore(HardSoftScore.of(0, -100));

        when(optimizationEngine.solve(any(Depot.class), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), anyInt())).thenReturn(solution);

        when(vehicleRepository.findById(replacementVehicle.getId())).thenReturn(Optional.of(replacementVehicle));
        when(orderRepository.findById(affectedOrder.getId())).thenReturn(Optional.of(affectedOrder));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            if (r.getId() == null) r.setId(UUID.randomUUID());
            return r;
        });

        RecoveryPlanResponse response = recoveryService.recoverIncident(testIncident.getId());

        assertThat(response).isNotNull();
        assertThat(response.isFeasible()).isTrue();
        assertThat(response.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(response.getReplacementVehicleCodes()).containsExactly("V-102");
        assertThat(response.getReassignedOrdersCount()).isEqualTo(1);
    }
}
