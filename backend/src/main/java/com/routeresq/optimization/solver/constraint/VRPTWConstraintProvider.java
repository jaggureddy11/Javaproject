package com.routeresq.optimization.solver.constraint;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import com.routeresq.optimization.solver.model.TimefoldCustomer;
import com.routeresq.optimization.solver.model.TimefoldVehicle;
import com.routeresq.shared.util.GeometryUtils;

public class VRPTWConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                vehicleCapacity(factory),
                timeWindowLateness(factory),
                driverShiftExceeded(factory),
                minimizeDistance(factory),
                returnToDepotDistance(factory),
                minimizeDuration(factory),
                minimizeVehicleCount(factory)
        };
    }

    public Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(customer -> customer.getVehicle() != null)
                .groupBy(TimefoldCustomer::getVehicle, ConstraintCollectors.sum(c -> c.getWeightKg().intValue()))
                .filter((vehicle, totalWeight) -> totalWeight > vehicle.getMaxWeightKg().intValue())
                .penalize(HardSoftScore.ONE_HARD, (vehicle, totalWeight) -> (totalWeight - vehicle.getMaxWeightKg().intValue()) * 1000)
                .asConstraint("H1: Vehicle Capacity Exceeded");
    }

    public Constraint timeWindowLateness(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(c -> {
                    Integer arrival = c.getArrivalTimeMinutes(null);
                    return arrival != null && arrival > c.getWindowEndMinutes();
                })
                .penalize(HardSoftScore.ONE_HARD, c -> {
                    Integer arrival = c.getArrivalTimeMinutes(null);
                    return (arrival - c.getWindowEndMinutes()) * 100;
                })
                .asConstraint("H2: Time Window Lateness");
    }

    public Constraint driverShiftExceeded(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(c -> {
                    TimefoldVehicle v = c.getVehicle();
                    Integer arrival = c.getArrivalTimeMinutes(null);
                    if (v == null || arrival == null) return false;
                    int returnTime = arrival + c.getServiceDurationMinutes();
                    return returnTime > v.getShiftEndMinutes();
                })
                .penalize(HardSoftScore.ONE_HARD, c -> {
                    TimefoldVehicle v = c.getVehicle();
                    Integer arrival = c.getArrivalTimeMinutes(null);
                    int returnTime = arrival + c.getServiceDurationMinutes();
                    return (returnTime - v.getShiftEndMinutes()) * 500;
                })
                .asConstraint("H3: Driver Shift Duration Exceeded");
    }

    public Constraint minimizeDistance(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(c -> c.getPreviousStandstill() != null)
                .penalize(HardSoftScore.ONE_SOFT, c -> {
                    double dist = GeometryUtils.haversineMeters(c.getPreviousStandstill().getLocation(), c.getLocation());
                    return (int) Math.round(dist);
                })
                .asConstraint("S1: Minimize Total Route Distance");
    }

    public Constraint returnToDepotDistance(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(c -> c.getNextCustomer() == null && c.getVehicle() != null)
                .penalize(HardSoftScore.ONE_SOFT, c -> {
                    double dist = GeometryUtils.haversineMeters(c.getLocation(), c.getVehicle().getDepotLocation());
                    return (int) Math.round(dist);
                })
                .asConstraint("S4: Return to Depot Distance");
    }

    public Constraint minimizeDuration(ConstraintFactory factory) {
        return factory.forEach(TimefoldCustomer.class)
                .filter(c -> c.getArrivalTimeMinutes(null) != null)
                .penalize(HardSoftScore.ONE_SOFT, c -> c.getServiceDurationMinutes() * 10)
                .asConstraint("S2: Minimize Travel & Service Duration");
    }

    public Constraint minimizeVehicleCount(ConstraintFactory factory) {
        return factory.forEach(TimefoldVehicle.class)
                .filter(v -> v.getNextCustomer() != null)
                .penalize(HardSoftScore.ONE_SOFT, v -> 5000) // Balanced 5km penalty per vehicle
                .asConstraint("S3: Minimize Vehicle Count");
    }
}
