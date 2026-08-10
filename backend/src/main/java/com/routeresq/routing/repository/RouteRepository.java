package com.routeresq.routing.repository;

import com.routeresq.routing.model.Route;
import com.routeresq.routing.model.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findByOptimizationRunId(UUID optimizationRunId);

    List<Route> findByVehicleId(UUID vehicleId);

    Optional<Route> findByVehicleIdAndStatus(UUID vehicleId, RouteStatus status);

    List<Route> findByStatus(RouteStatus status);
}
