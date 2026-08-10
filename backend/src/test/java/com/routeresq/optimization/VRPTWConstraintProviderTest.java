package com.routeresq.optimization;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.routeresq.optimization.solver.constraint.VRPTWConstraintProvider;
import com.routeresq.optimization.solver.model.RoutePlanSolution;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.shared.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

class VRPTWConstraintProviderTest {

    private ConstraintVerifier<VRPTWConstraintProvider, RoutePlanSolution> constraintVerifier;

    @BeforeEach
    void setUp() {
        constraintVerifier = ConstraintVerifier.build(
                new VRPTWConstraintProvider(),
                RoutePlanSolution.class,
                TimefoldCustomer.class
        );
    }

    @Test
    @DisplayName("H1: Vehicle Capacity Exceeded Constraint Activates On Overload")
    void testVehicleCapacityConstraint() {
        TimefoldVehicle vehicle = new TimefoldVehicle(
                UUID.randomUUID(), "TRUCK-01", UUID.randomUUID(),
                GeometryUtils.createPoint(41.8781, -87.6298),
                new BigDecimal("100.00"), // 100 kg max capacity
                480, 1020, UUID.randomUUID(), "Driver A"
        );

        TimefoldCustomer customer = new TimefoldCustomer(
                UUID.randomUUID(), "ORD-1", "Customer 1",
                GeometryUtils.createPoint(41.8850, -87.6300),
                new BigDecimal("120.00"), // 120 kg demand (overload by 20 kg)
                540, 660, 10, 1
        );
        customer.setPreviousStandstill(vehicle);

        constraintVerifier.verifyThat(VRPTWConstraintProvider::vehicleCapacity)
                .given(vehicle, customer)
                .penalizesBy(20000); // (120 - 100) * 1000 = 20000
    }
}
