package com.routeresq.incident.service;

import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.incident.dto.ImpactAnalysisResult;
import com.routeresq.incident.model.Incident;
import com.routeresq.incident.model.IncidentType;
import com.routeresq.order.model.Order;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.routing.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncidentImpactAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(IncidentImpactAnalyzer.class);

    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;

    public IncidentImpactAnalyzer(RouteRepository routeRepository, VehicleRepository vehicleRepository) {
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public ImpactAnalysisResult analyzeImpact(Incident incident) {
        Vehicle brokenVehicle = incident.getVehicle();
        if (brokenVehicle == null && incident.getOrder() != null && incident.getIncidentType() == IncidentType.URGENT_ORDER) {
            // Urgent order insertion scenario
            return analyzeUrgentOrderImpact(incident);
        }

        if (brokenVehicle == null) {
            return new ImpactAnalysisResult(
                    incident.getId(), null, "NONE", null, 0, 0,
                    List.of(), List.of(), List.of(), List.of(), false,
                    "No vehicle associated with incident"
            );
        }

        // Find active routes for broken vehicle
        List<Route> routes = routeRepository.findByVehicleId(brokenVehicle.getId());
        if (routes.isEmpty()) {
            return new ImpactAnalysisResult(
                    incident.getId(), brokenVehicle.getId(), brokenVehicle.getVehicleCode(), null, 0, 0,
                    List.of(), List.of(), List.of(), List.of(), false,
                    "No active route found for broken vehicle " + brokenVehicle.getVehicleCode()
            );
        }

        Route affectedRoute = routes.get(0);

        List<RouteStop> completedStops = new ArrayList<>();
        List<RouteStop> uncompletedStops = new ArrayList<>();

        for (RouteStop stop : affectedRoute.getStops()) {
            if (stop.getStopStatus() == StopStatus.COMPLETED) {
                completedStops.add(stop);
            } else {
                uncompletedStops.add(stop);
            }
        }

        List<Order> affectedOrders = uncompletedStops.stream()
                .filter(s -> s.getOrder() != null)
                .map(RouteStop::getOrder)
                .collect(Collectors.toList());

        List<UUID> affectedOrderIds = affectedOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<String> affectedOrderNumbers = affectedOrders.stream().map(Order::getOrderNumber).collect(Collectors.toList());

        // Find candidate replacement vehicles
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        List<Vehicle> candidateVehicles = allVehicles.stream()
                .filter(v -> !v.getId().equals(brokenVehicle.getId()))
                .filter(v -> v.getStatus() == VehicleStatus.IDLE || v.getStatus() == VehicleStatus.EN_ROUTE)
                .collect(Collectors.toList());

        List<UUID> candidateVehicleIds = candidateVehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        List<String> candidateVehicleCodes = candidateVehicles.stream().map(Vehicle::getVehicleCode).collect(Collectors.toList());

        boolean feasible = !candidateVehicles.isEmpty() && !affectedOrders.isEmpty();

        String msg = String.format(
                "Incident Impact Analyzed: %d completed stops preserved on route %s; %d orders affected; %d candidate replacement vehicles available.",
                completedStops.size(), affectedRoute.getId().toString().substring(0, 8),
                affectedOrders.size(), candidateVehicles.size()
        );

        return new ImpactAnalysisResult(
                incident.getId(),
                brokenVehicle.getId(),
                brokenVehicle.getVehicleCode(),
                affectedRoute.getId(),
                completedStops.size(),
                affectedOrders.size(),
                affectedOrderIds,
                affectedOrderNumbers,
                candidateVehicleIds,
                candidateVehicleCodes,
                feasible,
                msg
        );
    }

    private ImpactAnalysisResult analyzeUrgentOrderImpact(Incident incident) {
        Order urgentOrder = incident.getOrder();
        List<Vehicle> candidateVehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getStatus() == VehicleStatus.IDLE || v.getStatus() == VehicleStatus.EN_ROUTE)
                .collect(Collectors.toList());

        List<UUID> candidateVehicleIds = candidateVehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        List<String> candidateVehicleCodes = candidateVehicles.stream().map(Vehicle::getVehicleCode).collect(Collectors.toList());

        return new ImpactAnalysisResult(
                incident.getId(), null, "URGENT", null, 0, 1,
                List.of(urgentOrder.getId()), List.of(urgentOrder.getOrderNumber()),
                candidateVehicleIds, candidateVehicleCodes, true,
                "Urgent order " + urgentOrder.getOrderNumber() + " analyzed for insertion across " + candidateVehicles.size() + " candidate vehicles"
        );
    }
}
