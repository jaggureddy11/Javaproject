package com.routeresq.optimization.baseline.service;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.optimization.baseline.model.BaselineRoute;
import com.routeresq.optimization.baseline.model.BaselineRouteResult;
import com.routeresq.optimization.baseline.model.BaselineStop;
import com.routeresq.order.model.Order;
import com.routeresq.routing.provider.RoutingProvider;
import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BaselineRoutePlanner {

    public BaselineRouteResult planRoutes(Depot depot, List<Vehicle> vehicles, List<Order> orders, RoutingProvider routingProvider) {
        long startTimeMs = System.currentTimeMillis();

        BaselineRouteResult result = new BaselineRouteResult();
        Set<UUID> assignedOrderIds = new HashSet<>();
        List<Order> remainingOrders = new ArrayList<>(orders);

        // Sort orders deterministically by UUID for consistent tie-breaking
        remainingOrders.sort(Comparator.comparing(Order::getId));

        int totalDistanceMeters = 0;
        int totalDurationMinutes = 0;
        int vehiclesUsed = 0;
        int lateCount = 0;
        int capacityViolations = 0;
        int shiftViolations = 0;

        for (Vehicle vehicle : vehicles) {
            if (remainingOrders.isEmpty()) {
                break;
            }

            BaselineRoute route = new BaselineRoute(vehicle);
            Point currentLocation = depot.getLocation();
            int shiftStart = vehicle.getDriver() != null ? vehicle.getDriver().getShiftStartMinutes() : 480;
            int shiftEnd = vehicle.getDriver() != null ? vehicle.getDriver().getShiftEndMinutes() : 1020;

            int currentTime = shiftStart;
            double currentWeight = 0.0;
            double maxCapacity = vehicle.getMaxWeightKg() != null ? vehicle.getMaxWeightKg().doubleValue() : 500.0;

            double routeDistanceMeters = 0.0;
            int routeDurationMinutes = 0;
            int sequence = 1;

            while (!remainingOrders.isEmpty()) {
                // Find nearest feasible candidate
                Order bestCandidate = null;
                double bestDistance = Double.MAX_VALUE;
                boolean bestIsFullyFeasible = false;

                for (Order candidate : remainingOrders) {
                    double candidateWeight = candidate.getWeightKg() != null ? candidate.getWeightKg().doubleValue() : 10.0;
                    if (currentWeight + candidateWeight > maxCapacity) {
                        continue; // Cannot exceed capacity in greedy loop
                    }

                    double distanceMeters = GeometryUtils.haversineMeters(currentLocation, candidate.getLocation());
                    int travelMinutes = routingProvider.getTravelTimeMinutes(currentLocation, candidate.getLocation());
                    int arrivalMinutes = Math.max(candidate.getWindowStartMinutes(), currentTime + travelMinutes);
                    int departureMinutes = arrivalMinutes + candidate.getServiceDurationMinutes();

                    int returnTravelMinutes = routingProvider.getTravelTimeMinutes(candidate.getLocation(), depot.getLocation());
                    boolean isFeasible = (arrivalMinutes <= candidate.getWindowEndMinutes()) &&
                                         (departureMinutes + returnTravelMinutes <= shiftEnd);

                    if (isFeasible && (!bestIsFullyFeasible || distanceMeters < bestDistance)) {
                        bestCandidate = candidate;
                        bestDistance = distanceMeters;
                        bestIsFullyFeasible = true;
                    } else if (!bestIsFullyFeasible && distanceMeters < bestDistance) {
                        bestCandidate = candidate;
                        bestDistance = distanceMeters;
                    }
                }

                if (bestCandidate == null) {
                    break; // No further orders fit capacity for this vehicle
                }

                // Assign bestCandidate to this vehicle
                double dist = GeometryUtils.haversineMeters(currentLocation, bestCandidate.getLocation());
                int travelTime = routingProvider.getTravelTimeMinutes(currentLocation, bestCandidate.getLocation());

                int arrival = Math.max(bestCandidate.getWindowStartMinutes(), currentTime + travelTime);
                int departure = arrival + bestCandidate.getServiceDurationMinutes();
                boolean late = arrival > bestCandidate.getWindowEndMinutes();

                if (late) {
                    lateCount++;
                }

                BaselineStop stop = new BaselineStop(
                        UUID.randomUUID(),
                        bestCandidate,
                        sequence++,
                        arrival,
                        departure,
                        late
                );

                route.addStop(stop);
                currentWeight += bestCandidate.getWeightKg() != null ? bestCandidate.getWeightKg().doubleValue() : 10.0;
                routeDistanceMeters += dist;
                routeDurationMinutes += travelTime + bestCandidate.getServiceDurationMinutes();

                currentTime = departure;
                currentLocation = bestCandidate.getLocation();
                assignedOrderIds.add(bestCandidate.getId());
                remainingOrders.remove(bestCandidate);
            }

            if (!route.getStops().isEmpty()) {
                vehiclesUsed++;
                // Add return to depot leg
                double returnDist = GeometryUtils.haversineMeters(currentLocation, depot.getLocation());
                int returnTime = routingProvider.getTravelTimeMinutes(currentLocation, depot.getLocation());
                routeDistanceMeters += returnDist;
                routeDurationMinutes += returnTime;

                if (currentTime + returnTime > shiftEnd) {
                    shiftViolations++;
                }

                route.setTotalDistanceMeters((int) Math.round(routeDistanceMeters));
                route.setTotalDurationMinutes(routeDurationMinutes);

                totalDistanceMeters += routeDistanceMeters;
                totalDurationMinutes += routeDurationMinutes;

                result.getRoutes().add(route);
            }
        }

        long endTimeMs = System.currentTimeMillis();

        int unassignedCount = orders.size() - assignedOrderIds.size();
        boolean isFeasible = (unassignedCount == 0 && lateCount == 0 && shiftViolations == 0 && capacityViolations == 0);

        result.setTotalDistanceKm(new BigDecimal(totalDistanceMeters / 1000.0).setScale(2, RoundingMode.HALF_UP).doubleValue());
        result.setTotalDurationMinutes(totalDurationMinutes);
        result.setVehiclesUsed(vehiclesUsed);
        result.setOrdersAssigned(assignedOrderIds.size());
        result.setOrdersUnassigned(unassignedCount);
        result.setLateDeliveries(lateCount);
        result.setCapacityViolations(capacityViolations);
        result.setShiftViolations(shiftViolations);
        result.setFeasible(isFeasible);
        result.setExecutionTimeMs(endTimeMs - startTimeMs);

        return result;
    }
}
