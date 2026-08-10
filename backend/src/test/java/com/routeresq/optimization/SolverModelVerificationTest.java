package com.routeresq.optimization;

import com.routeresq.optimization.benchmark.model.BenchmarkDataset;
import com.routeresq.optimization.benchmark.service.BenchmarkDataGenerator;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.order.model.Order;
import com.routeresq.routing.matrix.DistanceMatrix;
import com.routeresq.routing.provider.HaversineRoutingProvider;
import com.routeresq.shared.util.GeometryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SolverModelVerificationTest {

    @Test
    @DisplayName("VERIFICATION 1: Benchmark Dataset SHA-256 Fingerprint Equality")
    void testDatasetFingerprintEquality() throws Exception {
        BenchmarkDataGenerator generator = new BenchmarkDataGenerator();

        BenchmarkDataGenerator.BenchmarkDataHolder d1 = generator.generateData(BenchmarkDataset.LARGE);
        BenchmarkDataGenerator.BenchmarkDataHolder d2 = generator.generateData(BenchmarkDataset.LARGE);

        String hash1 = computeFingerprint(d1.getOrders());
        String hash2 = computeFingerprint(d2.getOrders());

        assertThat(hash1).isEqualTo(hash2);
        assertThat(d1.getOrders().size()).isEqualTo(100);
        assertThat(d1.getVehicles().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("VERIFICATION 2: Dynamic Arrival Time Propagation Downstream in Chain (A -> B -> C)")
    void testArrivalTimePropagationDownstream() {
        Point depotLoc = GeometryUtils.createPoint(41.8781, -87.6298);
        Point locA = GeometryUtils.createPoint(41.8850, -87.6300);
        Point locB = GeometryUtils.createPoint(41.8900, -87.6350);

        List<Point> locs = List.of(depotLoc, locA, locB);
        DistanceMatrix matrix = DistanceMatrix.build(locs, new HaversineRoutingProvider());

        TimefoldVehicle vehicle = new TimefoldVehicle(
                UUID.randomUUID(), "V-1", UUID.randomUUID(), depotLoc,
                new BigDecimal("500.00"), 480, 1020, UUID.randomUUID(), "Driver A"
        );

        TimefoldCustomer customerA = new TimefoldCustomer(
                UUID.randomUUID(), "ORD-A", "Customer A", locA,
                new BigDecimal("20.00"), 540, 720, 10, 1
        );

        TimefoldCustomer customerB = new TimefoldCustomer(
                UUID.randomUUID(), "ORD-B", "Customer B", locB,
                new BigDecimal("30.00"), 540, 720, 10, 1
        );

        // Chain: vehicle -> customerA -> customerB
        customerA.setPreviousStandstill(vehicle);
        customerB.setPreviousStandstill(customerA);

        Integer arrivalA = customerA.getArrivalTimeMinutes(matrix);
        Integer arrivalB = customerB.getArrivalTimeMinutes(matrix);

        assertThat(arrivalA).isNotNull();
        assertThat(arrivalB).isNotNull();
        assertThat(arrivalA).isEqualTo(540); // Math.max(540, 480 + travelTime) -> 540

        int departureA = arrivalA + customerA.getServiceDurationMinutes();
        int travelTimeAB = matrix.getTravelTimeMinutes(locA, locB);
        int expectedArrivalB = Math.max(customerB.getWindowStartMinutes(), departureA + travelTimeAB);

        assertThat(arrivalB).isEqualTo(expectedArrivalB);
    }

    private String computeFingerprint(List<Order> orders) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Order o : orders) {
            digest.update(o.getId().toString().getBytes());
            digest.update(o.getLocation().toText().getBytes());
            digest.update(o.getWeightKg().toString().getBytes());
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
