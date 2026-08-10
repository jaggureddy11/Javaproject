package com.routeresq.domain;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStatus;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.shared.util.GeometryUtils;
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainUnitTest {

    @Test
    @DisplayName("User Entity Construction & Properties Test")
    void testUserEntity() {
        User user = User.builder()
                .email("dispatcher@routeresq.io")
                .passwordHash("hashed_secret")
                .firstName("John")
                .lastName("Dispatcher")
                .role(UserRole.DISPATCHER)
                .active(true)
                .build();

        assertThat(user.getEmail()).isEqualTo("dispatcher@routeresq.io");
        assertThat(user.getRole()).isEqualTo(UserRole.DISPATCHER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("GeometryUtils Point Creation & Extraction Test")
    void testGeometryUtils() {
        Point point = GeometryUtils.createPoint(41.8781, -87.6298);

        assertThat(point).isNotNull();
        assertThat(point.getSRID()).isEqualTo(4326);
        assertThat(GeometryUtils.getLatitude(point)).isEqualTo(41.8781);
        assertThat(GeometryUtils.getLongitude(point)).isEqualTo(-87.6298);

        assertThatThrownBy(() -> GeometryUtils.createPoint(95.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude must be between -90 and 90 degrees");

        assertThatThrownBy(() -> GeometryUtils.createPoint(0.0, -190.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude must be between -180 and 180 degrees");
    }

    @Test
    @DisplayName("Depot Entity Construction Test")
    void testDepotEntity() {
        Point point = GeometryUtils.createPoint(41.8781, -87.6298);
        Depot depot = Depot.builder()
                .name("Chicago Central")
                .location(point)
                .addressText("100 S Wacker Dr")
                .build();

        assertThat(depot.getName()).isEqualTo("Chicago Central");
        assertThat(depot.getLocation().getY()).isEqualTo(41.8781);
        assertThat(depot.getAddressText()).isEqualTo("100 S Wacker Dr");
    }

    @Test
    @DisplayName("Driver Entity Shift Validation Test")
    void testDriverEntityValidation() {
        Driver validDriver = Driver.builder()
                .name("Marcus Vance")
                .licenseNumber("DL-12345")
                .status(DriverStatus.ACTIVE)
                .shiftStartMinutes(480)
                .shiftEndMinutes(1020)
                .build();

        assertThat(validDriver.getShiftStartMinutes()).isEqualTo(480);
        assertThat(validDriver.getShiftEndMinutes()).isEqualTo(1020);

        Driver invalidDriver = Driver.builder()
                .name("Invalid Driver")
                .licenseNumber("DL-99999")
                .shiftStartMinutes(1020)
                .shiftEndMinutes(480)
                .build();

        assertThatThrownBy(invalidDriver::validateShift)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be strictly less than shift end minutes");
    }

    @Test
    @DisplayName("Order Entity Time Window Validation Test")
    void testOrderEntityValidation() {
        Point point = GeometryUtils.createPoint(41.8850, -87.6300);
        Depot depot = Depot.builder().name("Depot A").location(point).addressText("Address A").build();

        Order validOrder = Order.builder()
                .depot(depot)
                .orderNumber("ORD-101")
                .customerName("Acme")
                .location(point)
                .addressText("123 Main St")
                .weightKg(new BigDecimal("25.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(660)
                .status(OrderStatus.UNASSIGNED)
                .build();

        assertThat(validOrder.getWeightKg()).isEqualTo(new BigDecimal("25.00"));
        assertThat(validOrder.getStatus()).isEqualTo(OrderStatus.UNASSIGNED);

        Order invalidOrder = Order.builder()
                .depot(depot)
                .orderNumber("ORD-102")
                .customerName("Acme Bad")
                .location(point)
                .addressText("123 Main St")
                .weightKg(new BigDecimal("10.00"))
                .windowStartMinutes(700)
                .windowEndMinutes(600)
                .build();

        assertThatThrownBy(invalidOrder::validateTimeWindow)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be strictly less than window end minutes");
    }

    @Test
    @DisplayName("Route and RouteStop List Management Test")
    void testRouteStopManagement() {
        Vehicle vehicle = Vehicle.builder()
                .vehicleCode("TRUCK-01")
                .maxWeightKg(new BigDecimal("500.00"))
                .status(VehicleStatus.IDLE)
                .build();

        Route route = Route.builder()
                .vehicle(vehicle)
                .versionNumber(1)
                .status(RouteStatus.PLANNED)
                .build();

        RouteStop stop = RouteStop.builder()
                .sequenceNumber(1)
                .estimatedArrivalMinutes(560)
                .estimatedDepartureMinutes(570)
                .stopStatus(StopStatus.PENDING)
                .build();

        route.addStop(stop);

        assertThat(route.getStops()).hasSize(1);
        assertThat(stop.getRoute()).isEqualTo(route);

        route.removeStop(stop);
        assertThat(route.getStops()).isEmpty();
        assertThat(stop.getRoute()).isNull();
    }
}
