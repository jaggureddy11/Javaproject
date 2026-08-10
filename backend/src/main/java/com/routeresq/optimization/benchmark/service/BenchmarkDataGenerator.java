package com.routeresq.optimization.benchmark.service;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.optimization.benchmark.model.BenchmarkDataset;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class BenchmarkDataGenerator {

    public BenchmarkDataHolder generateData(BenchmarkDataset dataset) {
        Point depotLoc = GeometryUtils.createPoint(41.8781, -87.6298); // Chicago Loop
        Depot depot = Depot.builder()
                .name("Benchmark Depot (" + dataset.name() + ")")
                .location(depotLoc)
                .addressText("100 S Wacker Dr, Chicago, IL")
                .build();
        depot.setId(UUID.nameUUIDFromBytes(("depot-" + dataset.name()).getBytes()));

        int orderCount;
        int vehicleCount;

        switch (dataset) {
            case SMALL -> {
                orderCount = 5;
                vehicleCount = 2;
            }
            case MEDIUM -> {
                orderCount = 25;
                vehicleCount = 5;
            }
            case LARGE -> {
                orderCount = 100;
                vehicleCount = 10;
            }
            case TIGHT_TIME_WINDOWS -> {
                orderCount = 25;
                vehicleCount = 5;
            }
            case CAPACITY_PRESSURE -> {
                orderCount = 25;
                vehicleCount = 4;
            }
            case SPATIAL_CLUSTERING -> {
                orderCount = 30;
                vehicleCount = 4;
            }
            default -> {
                orderCount = 10;
                vehicleCount = 2;
            }
        }

        // Vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= vehicleCount; i++) {
            Driver driver = Driver.builder()
                    .name("Benchmark Driver " + i)
                    .licenseNumber("DL-BENCH-" + i)
                    .status(DriverStatus.ACTIVE)
                    .shiftStartMinutes(480) // 08:00
                    .shiftEndMinutes(1020)  // 17:00
                    .build();
            driver.setId(UUID.nameUUIDFromBytes(("driver-" + dataset.name() + "-" + i).getBytes()));

            BigDecimal maxWeight = (dataset == BenchmarkDataset.CAPACITY_PRESSURE)
                    ? new BigDecimal("120.00") // Tight capacity
                    : new BigDecimal("350.00");

            Vehicle vehicle = Vehicle.builder()
                    .vehicleCode("TRUCK-" + String.format("%02d", i))
                    .depot(depot)
                    .driver(driver)
                    .maxWeightKg(maxWeight)
                    .status(VehicleStatus.IDLE)
                    .currentLocation(depotLoc)
                    .build();
            vehicle.setId(UUID.nameUUIDFromBytes(("vehicle-" + dataset.name() + "-" + i).getBytes()));

            vehicles.add(vehicle);
        }

        // Deterministic Order Generator using fixed seed 42
        Random random = new Random(42);
        List<Order> orders = new ArrayList<>();

        if (dataset == BenchmarkDataset.SPATIAL_CLUSTERING) {
            // 3 Clusters: Loop (41.8781, -87.6298), O'Hare (41.9742, -87.9073), Hyde Park (41.7943, -87.5907)
            double[][] clusters = {
                    {41.8781, -87.6298},
                    {41.9742, -87.9073},
                    {41.7943, -87.5907}
            };
            for (int i = 1; i <= orderCount; i++) {
                double[] center = clusters[(i - 1) % 3];
                double lat = center[0] + (random.nextDouble() - 0.5) * 0.03;
                double lon = center[1] + (random.nextDouble() - 0.5) * 0.03;

                Order order = Order.builder()
                        .depot(depot)
                        .orderNumber("ORD-CLUSTER-" + String.format("%03d", i))
                        .customerName("Cluster Customer " + i)
                        .location(GeometryUtils.createPoint(lat, lon))
                        .addressText("Cluster Location " + i)
                        .weightKg(new BigDecimal("25.00"))
                        .windowStartMinutes(540) // 09:00
                        .windowEndMinutes(960)  // 16:00
                        .serviceDurationMinutes(10)
                        .priority(1)
                        .status(OrderStatus.UNASSIGNED)
                        .build();
                order.setId(UUID.nameUUIDFromBytes(("order-" + dataset.name() + "-" + i).getBytes()));
                orders.add(order);
            }
        } else {
            for (int i = 1; i <= orderCount; i++) {
                double lat = 41.8781 + (random.nextDouble() - 0.5) * 0.12;
                double lon = -87.6298 + (random.nextDouble() - 0.5) * 0.12;

                int windowStart = 540 + (i % 5) * 60; // 09:00, 10:00, ...
                int windowLength = (dataset == BenchmarkDataset.TIGHT_TIME_WINDOWS) ? 60 : 180; // 1-hr vs 3-hr
                int windowEnd = Math.min(1020, windowStart + windowLength);

                BigDecimal weight = (dataset == BenchmarkDataset.CAPACITY_PRESSURE)
                        ? new BigDecimal("20.00") // 25 orders * 20kg = 500kg total demand vs 480kg fleet capacity
                        : new BigDecimal(15 + random.nextInt(25));

                Order order = Order.builder()
                        .depot(depot)
                        .orderNumber("ORD-BENCH-" + String.format("%03d", i))
                        .customerName("Customer " + i)
                        .location(GeometryUtils.createPoint(lat, lon))
                        .addressText("Address " + i)
                        .weightKg(weight)
                        .windowStartMinutes(windowStart)
                        .windowEndMinutes(windowEnd)
                        .serviceDurationMinutes(10)
                        .priority(1 + (i % 5))
                        .status(OrderStatus.UNASSIGNED)
                        .build();
                order.setId(UUID.nameUUIDFromBytes(("order-" + dataset.name() + "-" + i).getBytes()));
                orders.add(order);
            }
        }

        return new BenchmarkDataHolder(depot, vehicles, orders);
    }

    public static class BenchmarkDataHolder {
        private final Depot depot;
        private final List<Vehicle> vehicles;
        private final List<Order> orders;

        public BenchmarkDataHolder(Depot depot, List<Vehicle> vehicles, List<Order> orders) {
            this.depot = depot;
            this.vehicles = vehicles;
            this.orders = orders;
        }

        public Depot getDepot() {
            return depot;
        }

        public List<Vehicle> getVehicles() {
            return vehicles;
        }

        public List<Order> getOrders() {
            return orders;
        }
    }
}
