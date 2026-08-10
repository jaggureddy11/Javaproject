package com.routeresq.optimization;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.optimization.baseline.model.BaselineRouteResult;
import com.routeresq.optimization.baseline.service.BaselineRoutePlanner;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.routing.provider.HaversineRoutingProvider;
import com.routeresq.shared.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaselineRoutePlannerTest {

    private BaselineRoutePlanner baselineRoutePlanner;
    private Depot depot;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        baselineRoutePlanner = new BaselineRoutePlanner();

        Point depotLocation = GeometryUtils.createPoint(41.8781, -87.6298);
        depot = Depot.builder()
                .name("Test Depot")
                .location(depotLocation)
                .addressText("Depot Address")
                .build();
        depot.setId(UUID.randomUUID());

        Driver driver = Driver.builder()
                .name("Test Driver")
                .licenseNumber("DL-100")
                .status(DriverStatus.ACTIVE)
                .shiftStartMinutes(480)
                .shiftEndMinutes(1020)
                .build();

        vehicle = Vehicle.builder()
                .vehicleCode("TRUCK-TEST")
                .depot(depot)
                .driver(driver)
                .maxWeightKg(new BigDecimal("200.00"))
                .status(VehicleStatus.IDLE)
                .currentLocation(depotLocation)
                .build();
        vehicle.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Baseline Planner selects nearest feasible order deterministically")
    void testNearestFeasibleSelection() {
        Order o1 = Order.builder()
                .depot(depot)
                .orderNumber("ORD-NEAR")
                .customerName("Near Customer")
                .location(GeometryUtils.createPoint(41.8790, -87.6300)) // ~100m away
                .weightKg(new BigDecimal("20.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(720)
                .serviceDurationMinutes(10)
                .status(OrderStatus.UNASSIGNED)
                .build();
        o1.setId(UUID.randomUUID());

        Order o2 = Order.builder()
                .depot(depot)
                .orderNumber("ORD-FAR")
                .customerName("Far Customer")
                .location(GeometryUtils.createPoint(41.8900, -87.6500)) // ~2km away
                .weightKg(new BigDecimal("20.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(720)
                .serviceDurationMinutes(10)
                .status(OrderStatus.UNASSIGNED)
                .build();
        o2.setId(UUID.randomUUID());

        List<Order> orders = List.of(o2, o1);

        BaselineRouteResult res1 = baselineRoutePlanner.planRoutes(depot, List.of(vehicle), orders, new HaversineRoutingProvider());
        BaselineRouteResult res2 = baselineRoutePlanner.planRoutes(depot, List.of(vehicle), orders, new HaversineRoutingProvider());

        assertThat(res1.getOrdersAssigned()).isEqualTo(2);
        assertThat(res1.getRoutes().get(0).getStops().get(0).getOrder().getOrderNumber()).isEqualTo("ORD-NEAR");
        assertThat(res1.getTotalDistanceKm()).isEqualTo(res2.getTotalDistanceKm());
    }

    @Test
    @DisplayName("Baseline Planner enforces vehicle max weight capacity")
    void testCapacityEnforcement() {
        Order o1 = Order.builder()
                .depot(depot)
                .orderNumber("ORD-1")
                .location(GeometryUtils.createPoint(41.8800, -87.6300))
                .weightKg(new BigDecimal("150.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(720)
                .serviceDurationMinutes(10)
                .build();
        o1.setId(UUID.randomUUID());

        Order o2 = Order.builder()
                .depot(depot)
                .orderNumber("ORD-2")
                .location(GeometryUtils.createPoint(41.8810, -87.6310))
                .weightKg(new BigDecimal("100.00")) // Total demand 250kg > vehicle capacity 200kg
                .windowStartMinutes(540)
                .windowEndMinutes(720)
                .serviceDurationMinutes(10)
                .build();
        o2.setId(UUID.randomUUID());

        BaselineRouteResult result = baselineRoutePlanner.planRoutes(depot, List.of(vehicle), List.of(o1, o2), new HaversineRoutingProvider());

        assertThat(result.getOrdersAssigned()).isEqualTo(1);
        assertThat(result.getOrdersUnassigned()).isEqualTo(1);
    }
}
