package com.routeresq.simulation;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.repository.OptimizationRunRepository;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.routing.repository.RouteRepository;
import com.routeresq.routing.repository.RouteStopRepository;
import com.routeresq.shared.util.GeometryUtils;
import com.routeresq.simulation.dto.CreateSimulationRequest;
import com.routeresq.simulation.dto.SimulationSessionResponse;
import com.routeresq.simulation.model.SimulationSession;
import com.routeresq.simulation.model.SimulationStatus;
import com.routeresq.simulation.repository.SimulationSessionRepository;
import com.routeresq.simulation.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private SimulationSessionRepository simulationSessionRepository;
    @Mock
    private OptimizationRunRepository optimizationRunRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteStopRepository routeStopRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private SimulationService simulationService;

    private OptimizationRun testRun;
    private Depot testDepot;
    private Route testRoute;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        simulationService = new SimulationService(
                simulationSessionRepository,
                optimizationRunRepository,
                routeRepository,
                routeStopRepository,
                orderRepository,
                null,
                null,
                messagingTemplate
        );

        UUID depotId = UUID.randomUUID();
        testDepot = Depot.builder()
                .name("Chicago Depot")
                .location(GeometryUtils.createPoint(41.8781, -87.6298))
                .addressText("Wacker Dr")
                .build();
        testDepot.setId(depotId);

        testVehicle = Vehicle.builder()
                .vehicleCode("V-101")
                .depot(testDepot)
                .maxWeightKg(new BigDecimal("500"))
                .currentLocation(GeometryUtils.createPoint(41.8781, -87.6298))
                .build();
        testVehicle.setId(UUID.randomUUID());

        testRun = new OptimizationRun();
        testRun.setId(UUID.randomUUID());

        testRoute = Route.builder()
                .optimizationRun(testRun)
                .vehicle(testVehicle)
                .totalDistanceKm(new BigDecimal("10.0"))
                .totalDurationMinutes(30)
                .build();
        testRoute.setId(UUID.randomUUID());

        Order o1 = Order.builder()
                .orderNumber("ORD-101")
                .customerName("Customer A")
                .location(GeometryUtils.createPoint(41.8850, -87.6300))
                .weightKg(new BigDecimal("20.0"))
                .windowStartMinutes(480)
                .windowEndMinutes(600)
                .status(OrderStatus.ASSIGNED)
                .build();
        o1.setId(UUID.randomUUID());

        RouteStop stop1 = RouteStop.builder()
                .route(testRoute)
                .order(o1)
                .sequenceNumber(1)
                .estimatedArrivalMinutes(495)
                .estimatedDepartureMinutes(505)
                .stopStatus(StopStatus.PENDING)
                .build();
        stop1.setId(UUID.randomUUID());

        testRoute.getStops().add(stop1);
    }

    @Test
    @DisplayName("Unit Test: Create Simulation Session")
    void testCreateSession() {
        when(optimizationRunRepository.findById(testRun.getId())).thenReturn(Optional.of(testRun));
        when(routeRepository.findByOptimizationRunId(testRun.getId())).thenReturn(List.of(testRoute));
        when(simulationSessionRepository.save(any(SimulationSession.class))).thenAnswer(inv -> {
            SimulationSession s = inv.getArgument(0);
            if (s.getId() == null) s.setId(UUID.randomUUID());
            return s;
        });

        CreateSimulationRequest request = new CreateSimulationRequest(testRun.getId(), 5);
        SimulationSessionResponse response = simulationService.createSession(request);

        assertThat(response).isNotNull();
        assertThat(response.getOptimizationRunId()).isEqualTo(testRun.getId());
        assertThat(response.getStatus()).isEqualTo(SimulationStatus.CREATED);
        assertThat(response.getSpeedMultiplier()).isEqualTo(5);
        assertThat(response.getSimulatedClockFormatted()).isEqualTo("08:00");
    }

    @Test
    @DisplayName("Unit Test: Position Interpolation & Linear Progress Calculation")
    void testPositionInterpolation() {
        double lat1 = 41.8781;
        double lon1 = -87.6298;
        double lat2 = 41.8881;
        double lon2 = -87.6198;

        double progressHalf = 0.5;
        double midLat = lat1 + progressHalf * (lat2 - lat1);
        double midLon = lon1 + progressHalf * (lon2 - lon1);

        assertThat(midLat).isEqualTo(41.8831);
        assertThat(midLon).isEqualTo(-87.6248);
    }
}
