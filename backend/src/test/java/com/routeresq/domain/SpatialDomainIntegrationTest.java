package com.routeresq.domain;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.fleet.repository.DriverRepository;
import com.routeresq.fleet.repository.VehicleRepository;
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
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import com.routeresq.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
@ActiveProfiles("test")
@Transactional
class SpatialDomainIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("routeresq_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    private Depot testDepot;

    @BeforeEach
    void setUp() {
        Point depotLocation = GeometryUtils.createPoint(41.8781, -87.6298); // Chicago Loop
        testDepot = depotRepository.save(Depot.builder()
                .name("Test Central Depot")
                .location(depotLocation)
                .addressText("100 S Wacker Dr, Chicago, IL 60606")
                .build());
    }

    @Test
    @DisplayName("User Persistence & Role Retrieval Test")
    void testUserPersistence() {
        User user = userRepository.save(User.builder()
                .email("test.dispatcher@routeresq.io")
                .passwordHash("$2a$12$encodedpassword")
                .firstName("Test")
                .lastName("Dispatcher")
                .role(UserRole.DISPATCHER)
                .active(true)
                .build());

        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();

        Optional<User> found = userRepository.findByEmail("test.dispatcher@routeresq.io");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.DISPATCHER);
    }

    @Test
    @DisplayName("PostGIS POINT Persistence & Distance Queries Test")
    void testSpatialOrderPersistenceAndQueries() {
        // Order 1: ~1 km away from Depot
        Point nearLocation = GeometryUtils.createPoint(41.8850, -87.6300);
        Order nearOrder = orderRepository.save(Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-NEAR-01")
                .customerName("Near Customer")
                .location(nearLocation)
                .addressText("Near Address")
                .weightKg(new BigDecimal("25.50"))
                .volumeM3(new BigDecimal("0.20"))
                .windowStartMinutes(540)
                .windowEndMinutes(660)
                .status(OrderStatus.UNASSIGNED)
                .build());

        // Order 2: ~50 km away from Depot (near Aurora, IL)
        Point farLocation = GeometryUtils.createPoint(41.7606, -88.3201);
        Order farOrder = orderRepository.save(Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-FAR-01")
                .customerName("Far Customer")
                .location(farLocation)
                .addressText("Far Address")
                .weightKg(new BigDecimal("10.00"))
                .volumeM3(new BigDecimal("0.10"))
                .windowStartMinutes(600)
                .windowEndMinutes(720)
                .status(OrderStatus.UNASSIGNED)
                .build());

        // Test spatial radius query: Orders within 5,000 meters (5 km) of Depot
        List<Order> nearbyOrders = orderRepository.findOrdersWithinRadius(testDepot.getLocation(), 5000.0);
        assertThat(nearbyOrders).hasSize(1);
        assertThat(nearbyOrders.get(0).getOrderNumber()).isEqualTo("ORD-NEAR-01");

        // Test spatial distance calculation (ST_DistanceSphere in meters)
        Double distanceMeters = orderRepository.calculateDistanceToOrder(nearOrder.getId(), testDepot.getLocation());
        assertThat(distanceMeters).isNotNull();
        assertThat(distanceMeters).isBetween(700.0, 1500.0);
    }

    @Test
    @DisplayName("Vehicle & Driver Relationship & Optimistic Locking Test")
    void testVehicleAndDriverPersistence() {
        Driver driver = driverRepository.save(Driver.builder()
                .name("John Doe")
                .licenseNumber("DL-TEST-99")
                .phone("+13125550000")
                .status(DriverStatus.ACTIVE)
                .shiftStartMinutes(480)
                .shiftEndMinutes(1020)
                .build());

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .vehicleCode("TRUCK-TEST-01")
                .depot(testDepot)
                .driver(driver)
                .maxWeightKg(new BigDecimal("500.00"))
                .status(VehicleStatus.IDLE)
                .currentLocation(testDepot.getLocation())
                .build());

        assertThat(vehicle.getId()).isNotNull();
        assertThat(vehicle.getVersion()).isNotNull();
        assertThat(vehicle.getDriver().getName()).isEqualTo("John Doe");

        // Query vehicle within 100 meters radius of depot
        List<Vehicle> nearbyVehicles = vehicleRepository.findVehiclesWithinRadius(testDepot.getLocation(), 100.0);
        assertThat(nearbyVehicles).extracting(Vehicle::getVehicleCode).contains("TRUCK-TEST-01");
    }

    @Test
    @DisplayName("Route & RouteStop Sequence & Uniqueness Test")
    void testRouteAndRouteStops() {
        Driver driver = driverRepository.save(Driver.builder()
                .name("Jane Smith")
                .licenseNumber("DL-TEST-88")
                .shiftStartMinutes(480)
                .shiftEndMinutes(1020)
                .build());

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .vehicleCode("TRUCK-TEST-02")
                .depot(testDepot)
                .driver(driver)
                .maxWeightKg(new BigDecimal("400.00"))
                .build());

        Order order = orderRepository.save(Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-STOP-01")
                .customerName("Stop Customer")
                .location(testDepot.getLocation())
                .addressText("Stop Address")
                .weightKg(new BigDecimal("15.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(660)
                .build());

        Route route = routeRepository.save(Route.builder()
                .vehicle(vehicle)
                .versionNumber(1)
                .status(RouteStatus.PLANNED)
                .build());

        RouteStop stop1 = RouteStop.builder()
                .order(order)
                .sequenceNumber(1)
                .estimatedArrivalMinutes(560)
                .estimatedDepartureMinutes(570)
                .stopStatus(StopStatus.PENDING)
                .build();

        route.addStop(stop1);
        routeRepository.save(route);

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderBySequenceNumberAsc(route.getId());
        assertThat(stops).hasSize(1);
        assertThat(stops.get(0).getSequenceNumber()).isEqualTo(1);
        assertThat(stops.get(0).getOrder().getOrderNumber()).isEqualTo("ORD-STOP-01");
    }

    @Test
    @DisplayName("Domain Validation Rules Test")
    void testDomainValidations() {
        Driver invalidDriver = Driver.builder()
                .name("Bad Driver")
                .licenseNumber("DL-BAD-01")
                .shiftStartMinutes(1000)
                .shiftEndMinutes(500) // Invalid: start > end
                .build();

        assertThatThrownBy(invalidDriver::validateShift)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be strictly less than shift end minutes");

        Order invalidOrder = Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-BAD-01")
                .customerName("Bad Order")
                .location(testDepot.getLocation())
                .addressText("Bad Address")
                .weightKg(new BigDecimal("10.00"))
                .windowStartMinutes(700)
                .windowEndMinutes(600) // Invalid: start > end
                .build();

        assertThatThrownBy(invalidOrder::validateTimeWindow)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be strictly less than window end minutes");
    }
}
