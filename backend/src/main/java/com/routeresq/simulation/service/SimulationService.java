package com.routeresq.simulation.service;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.repository.OptimizationRunRepository;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import com.routeresq.routing.provider.HaversineRoutingProvider;
import com.routeresq.routing.repository.RouteRepository;
import com.routeresq.routing.repository.RouteStopRepository;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.simulation.dto.CreateSimulationRequest;
import com.routeresq.simulation.dto.SimulationEvent;
import com.routeresq.simulation.dto.SimulationSessionResponse;
import com.routeresq.simulation.dto.SimulationVehicleStateDto;
import com.routeresq.simulation.model.SimVehicleStatus;
import com.routeresq.simulation.model.SimulationSession;
import com.routeresq.simulation.model.SimulationStatus;
import com.routeresq.simulation.repository.SimulationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final SimulationSessionRepository simulationSessionRepository;
    private final OptimizationRunRepository optimizationRunRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DepotRepository depotRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final HaversineRoutingProvider routingProvider = new HaversineRoutingProvider();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<UUID, InternalSimulationSession> activeSessions = new ConcurrentHashMap<>();

    public SimulationService(SimulationSessionRepository simulationSessionRepository,
                             OptimizationRunRepository optimizationRunRepository,
                             RouteRepository routeRepository,
                             RouteStopRepository routeStopRepository,
                             OrderRepository orderRepository,
                             VehicleRepository vehicleRepository,
                             DepotRepository depotRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.simulationSessionRepository = simulationSessionRepository;
        this.optimizationRunRepository = optimizationRunRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.depotRepository = depotRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public SimulationSessionResponse createSession(CreateSimulationRequest request) {
        OptimizationRun run = optimizationRunRepository.findById(request.getOptimizationRunId())
                .orElseThrow(() -> new ResourceNotFoundException("OptimizationRun", request.getOptimizationRunId()));

        List<Route> routes = routeRepository.findByOptimizationRunId(run.getId());
        if (routes.isEmpty()) {
            throw new IllegalArgumentException("Cannot simulate an optimization run with 0 planned routes");
        }

        // Check if running session already exists
        Optional<SimulationSession> existing = simulationSessionRepository.findByOptimizationRunIdAndStatus(run.getId(), SimulationStatus.RUNNING);
        if (existing.isPresent()) {
            throw new IllegalStateException("A running simulation session already exists for optimization run: " + run.getId());
        }

        Depot depot = routes.get(0).getVehicle().getDepot();

        SimulationSession session = new SimulationSession(
                run,
                SimulationStatus.CREATED,
                request.getSpeedMultiplier(),
                480.0, // 08:00 AM default
                null,
                null
        );

        SimulationSession saved = simulationSessionRepository.save(session);

        // Build in-memory state
        InternalSimulationSession internal = buildInternalSession(saved, routes, depot);
        activeSessions.put(saved.getId(), internal);

        return mapToResponse(internal);
    }

    @Transactional
    public SimulationSessionResponse startSession(UUID id) {
        InternalSimulationSession sessionState = activeSessions.get(id);
        if (sessionState == null) {
            SimulationSession saved = simulationSessionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SimulationSession", id));
            List<Route> routes = routeRepository.findByOptimizationRunId(saved.getOptimizationRun().getId());
            Depot depot = routes.isEmpty() ? null : routes.get(0).getVehicle().getDepot();
            sessionState = buildInternalSession(saved, routes, depot);
            activeSessions.put(id, sessionState);
        }

        final InternalSimulationSession internal = sessionState;

        synchronized (internal) {
            if (internal.status == SimulationStatus.RUNNING) {
                return mapToResponse(internal);
            }

            internal.status = SimulationStatus.RUNNING;
            internal.startedAt = Instant.now();

            // Persist status
            SimulationSession entity = simulationSessionRepository.findById(id).orElse(null);
            if (entity != null) {
                entity.setStatus(SimulationStatus.RUNNING);
                entity.setStartedAt(internal.startedAt);
                simulationSessionRepository.save(entity);
            }

            // Broadcast SIMULATION_STARTED
            broadcastEvent(internal, "SIMULATION_STARTED", "Simulation started at 08:00 AM");

            final UUID targetId = internal.id;
            // Schedule tick loop every 250ms
            if (internal.scheduledFuture == null || internal.scheduledFuture.isCancelled()) {
                internal.scheduledFuture = scheduler.scheduleAtFixedRate(
                        () -> tickSession(targetId),
                        0, 250, TimeUnit.MILLISECONDS
                );
            }
        }

        return mapToResponse(internal);
    }

    @Transactional
    public SimulationSessionResponse pauseSession(UUID id) {
        InternalSimulationSession internal = activeSessions.get(id);
        if (internal == null) throw new ResourceNotFoundException("SimulationSession", id);

        synchronized (internal) {
            if (internal.status == SimulationStatus.RUNNING) {
                internal.status = SimulationStatus.PAUSED;
                if (internal.scheduledFuture != null) {
                    internal.scheduledFuture.cancel(false);
                    internal.scheduledFuture = null;
                }

                SimulationSession entity = simulationSessionRepository.findById(id).orElse(null);
                if (entity != null) {
                    entity.setStatus(SimulationStatus.PAUSED);
                    simulationSessionRepository.save(entity);
                }

                broadcastEvent(internal, "SIMULATION_PAUSED", "Simulation paused at " + formatClock(internal.simulatedCurrentTimeMinutes));
            }
        }

        return mapToResponse(internal);
    }

    @Transactional
    public SimulationSessionResponse resumeSession(UUID id) {
        InternalSimulationSession internal = activeSessions.get(id);
        if (internal == null) throw new ResourceNotFoundException("SimulationSession", id);

        synchronized (internal) {
            if (internal.status == SimulationStatus.PAUSED) {
                internal.status = SimulationStatus.RUNNING;

                SimulationSession entity = simulationSessionRepository.findById(id).orElse(null);
                if (entity != null) {
                    entity.setStatus(SimulationStatus.RUNNING);
                    simulationSessionRepository.save(entity);
                }

                final UUID targetId = internal.id;
                if (internal.scheduledFuture == null || internal.scheduledFuture.isCancelled()) {
                    internal.scheduledFuture = scheduler.scheduleAtFixedRate(
                            () -> tickSession(targetId),
                            0, 250, TimeUnit.MILLISECONDS
                    );
                }
            }
        }

        return mapToResponse(internal);
    }

    @Transactional
    public SimulationSessionResponse stopSession(UUID id) {
        InternalSimulationSession internal = activeSessions.get(id);
        if (internal == null) throw new ResourceNotFoundException("SimulationSession", id);

        synchronized (internal) {
            internal.status = SimulationStatus.STOPPED;
            internal.completedAt = Instant.now();

            if (internal.scheduledFuture != null) {
                internal.scheduledFuture.cancel(false);
                internal.scheduledFuture = null;
            }

            SimulationSession entity = simulationSessionRepository.findById(id).orElse(null);
            if (entity != null) {
                entity.setStatus(SimulationStatus.STOPPED);
                entity.setCompletedAt(internal.completedAt);
                simulationSessionRepository.save(entity);
            }

            broadcastEvent(internal, "SIMULATION_STOPPED", "Simulation stopped manually");
        }

        return mapToResponse(internal);
    }

    @Transactional
    public void applyRecoveryPlan(UUID simulationId, UUID brokenVehicleId, List<Route> replacementRoutes) {
        InternalSimulationSession internal = activeSessions.get(simulationId);
        if (internal == null) return;

        synchronized (internal) {
            // 1. Halt broken vehicle
            if (brokenVehicleId != null) {
                for (InternalVehicleState v : internal.vehicles) {
                    if (v.vehicleId.equals(brokenVehicleId)) {
                        v.status = SimVehicleStatus.COMPLETED;
                        break;
                    }
                }
            }

            // 2. Add or update replacement routes
            for (Route r : replacementRoutes) {
                UUID replacementVehicleId = r.getVehicle().getId();
                InternalVehicleState existing = internal.vehicles.stream()
                        .filter(v -> v.vehicleId.equals(replacementVehicleId))
                        .findFirst().orElse(null);

                if (existing != null) {
                    List<InternalStop> newStops = new ArrayList<>();
                    double prevLat = existing.latitude;
                    double prevLon = existing.longitude;

                    List<RouteStop> sorted = r.getStops().stream()
                            .sorted((a, b) -> Integer.compare(a.getSequenceNumber(), b.getSequenceNumber()))
                            .collect(Collectors.toList());

                    for (RouteStop rs : sorted) {
                        if (rs.getOrder() == null) continue;
                        InternalStop s = new InternalStop();
                        s.stopId = rs.getId();
                        s.orderId = rs.getOrder().getId();
                        s.orderNumber = rs.getOrder().getOrderNumber();
                        s.customerName = rs.getOrder().getCustomerName();
                        s.latitude = com.routeresq.shared.util.GeometryUtils.getLatitude(rs.getOrder().getLocation());
                        s.longitude = com.routeresq.shared.util.GeometryUtils.getLongitude(rs.getOrder().getLocation());
                        s.windowStartMinutes = rs.getOrder().getWindowStartMinutes();
                        s.windowEndMinutes = rs.getOrder().getWindowEndMinutes();
                        s.serviceDurationMinutes = rs.getOrder().getServiceDurationMinutes() > 0 ? rs.getOrder().getServiceDurationMinutes() : 10;

                        double segDist = com.routeresq.shared.util.GeometryUtils.haversineMeters(prevLat, prevLon, s.latitude, s.longitude);
                        s.distanceKmFromPrev = segDist / 1000.0;
                        s.travelMinutesFromPrev = (s.distanceKmFromPrev / 30.0) * 60.0;

                        newStops.add(s);
                        prevLat = s.latitude;
                        prevLon = s.longitude;
                    }

                    existing.stops = newStops;
                    existing.totalStops = newStops.size();
                    existing.currentStopIndex = 0;
                    existing.status = SimVehicleStatus.EN_ROUTE;
                    existing.segmentStartLat = existing.latitude;
                    existing.segmentStartLon = existing.longitude;
                    existing.segmentElapsedMinutes = 0;
                }
            }

            broadcastEvent(internal, "ROUTE_REPLANNED", "Simulation updated with dynamic recovery routes");
        }
    }

    public SimulationSessionResponse getSessionState(UUID id) {
        InternalSimulationSession internal = activeSessions.get(id);
        if (internal != null) {
            return mapToResponse(internal);
        }

        SimulationSession saved = simulationSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SimulationSession", id));
        List<Route> routes = routeRepository.findByOptimizationRunId(saved.getOptimizationRun().getId());
        Depot depot = routes.isEmpty() ? null : routes.get(0).getVehicle().getDepot();
        InternalSimulationSession created = buildInternalSession(saved, routes, depot);
        return mapToResponse(created);
    }

    // ── Timer Tick Execution Logic ──────────────────────────────
    private void tickSession(UUID simulationId) {
        InternalSimulationSession internal = activeSessions.get(simulationId);
        if (internal == null || internal.status != SimulationStatus.RUNNING) return;

        synchronized (internal) {
            // Calculate simulated time increment: 250ms real = (speedMultiplier * 0.25) / 60 min simulated
            double minutesIncrement = (internal.speedMultiplier * 0.25) / 60.0;
            internal.simulatedCurrentTimeMinutes += minutesIncrement;

            boolean allVehiclesCompleted = true;

            for (InternalVehicleState vState : internal.vehicles) {
                if (vState.status == SimVehicleStatus.COMPLETED) continue;
                allVehiclesCompleted = false;

                advanceVehicle(internal, vState, minutesIncrement);
            }

            // Broadcast high-frequency position envelope
            broadcastEvent(internal, "VEHICLE_POSITION_UPDATED", internal.vehicles.stream().map(this::mapVehicleDto).collect(Collectors.toList()));

            // Check session completion
            if (allVehiclesCompleted && internal.vehicles.size() > 0) {
                internal.status = SimulationStatus.COMPLETED;
                internal.completedAt = Instant.now();

                if (internal.scheduledFuture != null) {
                    internal.scheduledFuture.cancel(false);
                    internal.scheduledFuture = null;
                }

                broadcastEvent(internal, "SIMULATION_COMPLETED", "All routes and deliveries completed");

                // Update DB session
                SimulationSession entity = simulationSessionRepository.findById(simulationId).orElse(null);
                if (entity != null) {
                    entity.setStatus(SimulationStatus.COMPLETED);
                    entity.setCompletedAt(internal.completedAt);
                    simulationSessionRepository.save(entity);
                }
            }
        }
    }

    private void advanceVehicle(InternalSimulationSession internal, InternalVehicleState vState, double deltaMinutes) {
        if (vState.stops.isEmpty()) {
            vState.status = SimVehicleStatus.COMPLETED;
            return;
        }

        double currentTime = internal.simulatedCurrentTimeMinutes;

        // Current target stop
        if (vState.currentStopIndex < vState.stops.size()) {
            InternalStop targetStop = vState.stops.get(vState.currentStopIndex);

            // Starting departure from depot or previous stop
            if (vState.status == SimVehicleStatus.AT_DEPOT) {
                if (currentTime >= 480.0) { // 08:00 AM
                    vState.status = SimVehicleStatus.EN_ROUTE;
                    broadcastEvent(internal, "VEHICLE_DEPARTED", "Vehicle " + vState.vehicleCode + " departed depot");
                } else {
                    return;
                }
            }

            if (vState.status == SimVehicleStatus.EN_ROUTE) {
                // Advance travel along segment
                double segmentDurationMin = Math.max(targetStop.travelMinutesFromPrev, 1.0);
                vState.segmentElapsedMinutes += deltaMinutes;
                double progress = Math.min(vState.segmentElapsedMinutes / segmentDurationMin, 1.0);

                // Interpolate coords
                vState.latitude = vState.segmentStartLat + progress * (targetStop.latitude - vState.segmentStartLat);
                vState.longitude = vState.segmentStartLon + progress * (targetStop.longitude - vState.segmentStartLon);
                vState.distanceTravelledKm += (progress * targetStop.distanceKmFromPrev);

                if (progress >= 1.0) {
                    // Arrived at stop
                    vState.status = SimVehicleStatus.ARRIVED;
                    vState.arrivedTimeMinutes = currentTime;

                    // Check time window lateness
                    if (targetStop.windowEndMinutes != null && currentTime > targetStop.windowEndMinutes) {
                        internal.lateDeliveriesCount++;
                        targetStop.isLate = true;
                    }

                    broadcastEvent(internal, "VEHICLE_ARRIVED", "Vehicle " + vState.vehicleCode + " arrived at " + targetStop.customerName);
                }
            }

            if (vState.status == SimVehicleStatus.ARRIVED) {
                // Wait for time window start if early
                if (targetStop.windowStartMinutes != null && currentTime < targetStop.windowStartMinutes) {
                    // Waiting at customer
                    return;
                }
                vState.status = SimVehicleStatus.SERVICING;
                vState.serviceElapsedMinutes = 0;
            }

            if (vState.status == SimVehicleStatus.SERVICING) {
                vState.serviceElapsedMinutes += deltaMinutes;
                if (vState.serviceElapsedMinutes >= targetStop.serviceDurationMinutes) {
                    // Delivery completed!
                    targetStop.completed = true;
                    internal.completedDeliveriesCount++;

                    broadcastEvent(internal, "ORDER_DELIVERED", Map.of(
                            "orderId", targetStop.orderId,
                            "orderNumber", targetStop.orderNumber,
                            "customerName", targetStop.customerName,
                            "vehicleCode", vState.vehicleCode,
                            "deliveredTime", formatClock(currentTime)
                    ));

                    // Move to next stop
                    vState.currentStopIndex++;
                    if (vState.currentStopIndex < vState.stops.size()) {
                        InternalStop nextStop = vState.stops.get(vState.currentStopIndex);
                        vState.status = SimVehicleStatus.EN_ROUTE;
                        vState.segmentStartLat = targetStop.latitude;
                        vState.segmentStartLon = targetStop.longitude;
                        vState.segmentElapsedMinutes = 0;
                    } else {
                        // All stops complete -> Return to depot
                        vState.status = SimVehicleStatus.RETURNING;
                        vState.segmentStartLat = targetStop.latitude;
                        vState.segmentStartLon = targetStop.longitude;
                        vState.segmentElapsedMinutes = 0;
                    }
                }
            }
        } else if (vState.status == SimVehicleStatus.RETURNING) {
            // Traveling back to depot
            double returnDistKm = com.routeresq.shared.util.GeometryUtils.haversineMeters(
                    vState.segmentStartLat, vState.segmentStartLon,
                    internal.depotLat, internal.depotLon
            ) / 1000.0;
            double returnDurationMin = Math.max((returnDistKm / 30.0) * 60.0, 1.0);

            vState.segmentElapsedMinutes += deltaMinutes;
            double progress = Math.min(vState.segmentElapsedMinutes / returnDurationMin, 1.0);

            vState.latitude = vState.segmentStartLat + progress * (internal.depotLat - vState.segmentStartLat);
            vState.longitude = vState.segmentStartLon + progress * (internal.depotLon - vState.segmentStartLon);

            if (progress >= 1.0) {
                vState.status = SimVehicleStatus.COMPLETED;
                vState.latitude = internal.depotLat;
                vState.longitude = internal.depotLon;
                broadcastEvent(internal, "ROUTE_COMPLETED", "Route completed for vehicle " + vState.vehicleCode);
            }
        }
    }

    // ── Helper Constructors & Builders ──────────────────────────
    private InternalSimulationSession buildInternalSession(SimulationSession session, List<Route> routes, Depot depot) {
        InternalSimulationSession internal = new InternalSimulationSession();
        internal.id = session.getId();
        internal.optimizationRunId = session.getOptimizationRun().getId();
        internal.status = session.getStatus();
        internal.speedMultiplier = session.getSpeedMultiplier();
        internal.simulatedCurrentTimeMinutes = session.getSimulatedCurrentTimeMinutes();
        internal.depotLat = depot != null && depot.getLocation() != null ? com.routeresq.shared.util.GeometryUtils.getLatitude(depot.getLocation()) : 41.8781;
        internal.depotLon = depot != null && depot.getLocation() != null ? com.routeresq.shared.util.GeometryUtils.getLongitude(depot.getLocation()) : -87.6298;
        internal.createdAt = session.getCreatedAt() != null ? session.getCreatedAt() : Instant.now();

        int totalDeliveries = 0;

        for (Route route : routes) {
            InternalVehicleState vState = new InternalVehicleState();
            vState.vehicleId = route.getVehicle().getId();
            vState.vehicleCode = route.getVehicle().getVehicleCode();
            vState.driverName = route.getVehicle().getDriver() != null ? route.getVehicle().getDriver().getName() : "Driver";
            vState.routeId = route.getId();
            vState.status = SimVehicleStatus.AT_DEPOT;
            vState.latitude = internal.depotLat;
            vState.longitude = internal.depotLon;
            vState.segmentStartLat = internal.depotLat;
            vState.segmentStartLon = internal.depotLon;

            double prevLat = internal.depotLat;
            double prevLon = internal.depotLon;

            List<RouteStop> sortedStops = route.getStops().stream()
                    .sorted((a, b) -> Integer.compare(a.getSequenceNumber(), b.getSequenceNumber()))
                    .collect(Collectors.toList());

            for (RouteStop stop : sortedStops) {
                if (stop.getOrder() == null) continue;
                totalDeliveries++;

                InternalStop s = new InternalStop();
                s.stopId = stop.getId();
                s.orderId = stop.getOrder().getId();
                s.orderNumber = stop.getOrder().getOrderNumber();
                s.customerName = stop.getOrder().getCustomerName();
                s.latitude = com.routeresq.shared.util.GeometryUtils.getLatitude(stop.getOrder().getLocation());
                s.longitude = com.routeresq.shared.util.GeometryUtils.getLongitude(stop.getOrder().getLocation());
                s.windowStartMinutes = stop.getOrder().getWindowStartMinutes();
                s.windowEndMinutes = stop.getOrder().getWindowEndMinutes();
                s.serviceDurationMinutes = stop.getOrder().getServiceDurationMinutes() > 0 ? stop.getOrder().getServiceDurationMinutes() : 10;

                double segDistMeters = com.routeresq.shared.util.GeometryUtils.haversineMeters(prevLat, prevLon, s.latitude, s.longitude);
                s.distanceKmFromPrev = segDistMeters / 1000.0;
                s.travelMinutesFromPrev = (s.distanceKmFromPrev / 30.0) * 60.0; // 30 km/h urban speed

                vState.stops.add(s);
                prevLat = s.latitude;
                prevLon = s.longitude;
            }

            vState.totalStops = vState.stops.size();
            internal.vehicles.add(vState);
        }

        internal.totalDeliveriesCount = totalDeliveries;
        return internal;
    }

    private void broadcastEvent(InternalSimulationSession session, String eventType, Object payload) {
        SimulationEvent event = new SimulationEvent(
                eventType,
                session.id,
                session.simulatedCurrentTimeMinutes,
                formatClock(session.simulatedCurrentTimeMinutes),
                payload
        );
        messagingTemplate.convertAndSend("/topic/simulation/" + session.id, event);
    }

    private SimulationSessionResponse mapToResponse(InternalSimulationSession session) {
        List<SimulationVehicleStateDto> vDtos = session.vehicles.stream().map(this::mapVehicleDto).collect(Collectors.toList());
        return new SimulationSessionResponse(
                session.id,
                session.optimizationRunId,
                session.status,
                session.speedMultiplier,
                session.simulatedCurrentTimeMinutes,
                formatClock(session.simulatedCurrentTimeMinutes),
                session.vehicles.size(),
                session.totalDeliveriesCount,
                session.completedDeliveriesCount,
                session.lateDeliveriesCount,
                session.vehicles.stream().mapToDouble(v -> v.distanceTravelledKm).sum(),
                vDtos,
                session.createdAt,
                session.startedAt,
                session.completedAt
        );
    }

    private SimulationVehicleStateDto mapVehicleDto(InternalVehicleState v) {
        InternalStop curStop = (v.currentStopIndex < v.stops.size()) ? v.stops.get(v.currentStopIndex) : null;
        return new SimulationVehicleStateDto(
                v.vehicleId,
                v.vehicleCode,
                v.driverName,
                v.routeId,
                v.status,
                v.latitude,
                v.longitude,
                v.currentStopIndex + 1,
                v.totalStops,
                curStop != null ? curStop.orderId : null,
                curStop != null ? curStop.orderNumber : null,
                curStop != null ? curStop.customerName : null,
                v.distanceTravelledKm,
                0.0,
                curStop != null ? (int) Math.round(curStop.travelMinutesFromPrev) : null
        );
    }

    private static String formatClock(double totalMinutes) {
        int mins = (int) Math.floor(totalMinutes);
        int h = (mins / 60) % 24;
        int m = mins % 60;
        return String.format("%02d:%02d", h, m);
    }

    // ── Internal In-Memory State Classes ────────────────────────
    private static class InternalSimulationSession {
        UUID id;
        UUID optimizationRunId;
        SimulationStatus status = SimulationStatus.CREATED;
        int speedMultiplier = 5;
        double simulatedCurrentTimeMinutes = 480.0; // 08:00 AM
        double depotLat;
        double depotLon;
        int totalDeliveriesCount = 0;
        int completedDeliveriesCount = 0;
        int lateDeliveriesCount = 0;
        Instant createdAt;
        Instant startedAt;
        Instant completedAt;
        ScheduledFuture<?> scheduledFuture;
        List<InternalVehicleState> vehicles = new ArrayList<>();
    }

    private static class InternalVehicleState {
        UUID vehicleId;
        String vehicleCode;
        String driverName;
        UUID routeId;
        SimVehicleStatus status = SimVehicleStatus.AT_DEPOT;
        double latitude;
        double longitude;
        double segmentStartLat;
        double segmentStartLon;
        double segmentElapsedMinutes = 0;
        double serviceElapsedMinutes = 0;
        double arrivedTimeMinutes = 0;
        double distanceTravelledKm = 0;
        int currentStopIndex = 0;
        int totalStops = 0;
        List<InternalStop> stops = new ArrayList<>();
    }

    private static class InternalStop {
        UUID stopId;
        UUID orderId;
        String orderNumber;
        String customerName;
        double latitude;
        double longitude;
        Integer windowStartMinutes;
        Integer windowEndMinutes;
        int serviceDurationMinutes = 10;
        double distanceKmFromPrev;
        double travelMinutesFromPrev;
        boolean completed = false;
        boolean isLate = false;
    }
}
