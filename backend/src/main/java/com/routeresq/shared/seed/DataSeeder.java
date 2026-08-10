package com.routeresq.shared.seed;

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
import com.routeresq.shared.util.GeometryUtils;
import com.routeresq.user.model.User;
import com.routeresq.user.model.UserRole;
import com.routeresq.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DepotRepository depotRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      DepotRepository depotRepository,
                      DriverRepository driverRepository,
                      VehicleRepository vehicleRepository,
                      OrderRepository orderRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.depotRepository = depotRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (depotRepository.count() > 0) {
            log.info("Database already contains seed data. Skipping DataSeeder.");
            return;
        }

        log.info("Seeding initial development dataset for Greater Chicago Region...");

        // 1. Seed Development Users (Idempotent check)
        User admin = seedUserIfNotFound("admin@routeresq.io", "admin123", "System", "Admin", UserRole.ADMIN);
        User dispatcher = seedUserIfNotFound("dispatcher@routeresq.io", "dispatch123", "Lead", "Dispatcher", UserRole.DISPATCHER);
        User driverUser = seedUserIfNotFound("driver@routeresq.io", "driver123", "Marcus", "Vance", UserRole.DRIVER);

        log.info("Seeded development users: admin ({}), dispatcher ({}), driver ({})", 
                admin.getEmail(), dispatcher.getEmail(), driverUser.getEmail());

        // 2. Seed Depots (Chicago area)
        Depot centralDepot = depotRepository.save(Depot.builder()
                .name("Chicago Central Depot")
                .location(GeometryUtils.createPoint(41.8781, -87.6298))
                .addressText("100 S Wacker Dr, Chicago, IL 60606")
                .build());

        Depot ohareDepot = depotRepository.save(Depot.builder()
                .name("O'Hare Freight Hub")
                .location(GeometryUtils.createPoint(41.9742, -87.9073))
                .addressText("10000 Bessie Coleman Dr, Chicago, IL 60666")
                .build());

        Depot midwayDepot = depotRepository.save(Depot.builder()
                .name("Midway Logistics Center")
                .location(GeometryUtils.createPoint(41.7868, -87.7522))
                .addressText("5700 S Cicero Ave, Chicago, IL 60638")
                .build());

        log.info("Seeded 3 depots: {}, {}, {}", centralDepot.getName(), ohareDepot.getName(), midwayDepot.getName());

        // 3. Seed Drivers
        List<Driver> drivers = new ArrayList<>();
        String[] driverNames = {
                "Marcus Vance", "Elena Rostova", "David Chen", "Sarah Jenkins", "Robert Miller",
                "Anita Patel", "Carlos Gomez", "James Wilson", "Maya Lin", "Thomas Wright"
        };
        for (int i = 0; i < driverNames.length; i++) {
            Driver driver = driverRepository.save(Driver.builder()
                    .name(driverNames[i])
                    .licenseNumber("DL-IL-2026-" + (1000 + i))
                    .phone("+1-312-555-" + String.format("%04d", 100 + i))
                    .status(DriverStatus.ACTIVE)
                    .shiftStartMinutes(480) // 08:00
                    .shiftEndMinutes(1020)  // 17:00
                    .build());
            drivers.add(driver);
        }
        log.info("Seeded {} drivers", drivers.size());

        // 4. Seed Vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Depot homeDepot = (i < 6) ? centralDepot : (i < 8 ? ohareDepot : midwayDepot);
            Driver driver = drivers.get(i);
            BigDecimal capacity = new BigDecimal(300 + (i * 50)); // 300kg to 750kg

            Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                    .vehicleCode("TRUCK-" + String.format("%02d", i + 1))
                    .depot(homeDepot)
                    .driver(driver)
                    .maxWeightKg(capacity)
                    .maxVolumeM3(new BigDecimal("12.50"))
                    .status(VehicleStatus.IDLE)
                    .currentLocation(homeDepot.getLocation())
                    .build());
            vehicles.add(vehicle);
        }
        log.info("Seeded {} fleet vehicles", vehicles.size());

        // 5. Seed 50 Realistic Chicago Delivery Orders surrounding Central Depot
        Random random = new Random(42); // Deterministic seed for reproducible testing
        double baseLat = 41.8781;
        double baseLon = -87.6298;

        String[] customerPrefixes = {"Acme Corp", "Apex Logistics", "Beacon Retail", "Crestline Health", "Delta Supply", "EcoGoods"};
        int[][] windows = {
                {540, 660},  // 09:00 - 11:00
                {600, 720},  // 10:00 - 12:00
                {780, 900},  // 13:00 - 15:00
                {840, 1020}  // 14:00 - 17:00
        };

        for (int i = 1; i <= 50; i++) {
            double latOffset = (random.nextDouble() - 0.5) * 0.14;
            double lonOffset = (random.nextDouble() - 0.5) * 0.18;
            double orderLat = baseLat + latOffset;
            double orderLon = baseLon + lonOffset;

            int[] window = windows[i % windows.length];
            BigDecimal weight = new BigDecimal(5 + random.nextInt(40)).setScale(2, RoundingMode.HALF_UP);
            String customer = customerPrefixes[i % customerPrefixes.length] + " #" + i;

            orderRepository.save(Order.builder()
                    .orderNumber("ORD-" + String.format("%04d", 1000 + i))
                    .depot(centralDepot)
                    .customerName(customer)
                    .location(GeometryUtils.createPoint(orderLat, orderLon))
                    .addressText(String.format("%.4f N, %.4f W, Chicago Area", orderLat, Math.abs(orderLon)))
                    .weightKg(weight)
                    .volumeM3(new BigDecimal("0.25"))
                    .windowStartMinutes(window[0])
                    .windowEndMinutes(window[1])
                    .serviceDurationMinutes(10)
                    .status(OrderStatus.UNASSIGNED)
                    .build());
        }
        log.info("Seeded 50 delivery orders around Greater Chicago Area.");
        log.info("DataSeeder completed successfully.");
    }

    private User seedUserIfNotFound(String email, String rawPassword, String firstName, String lastName, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .active(true)
                .build()));
    }
}
