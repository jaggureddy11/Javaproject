package com.routeresq.routing.repository;

import com.routeresq.routing.model.RouteStop;
import com.routeresq.routing.model.StopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    List<RouteStop> findByRouteIdOrderBySequenceNumberAsc(UUID routeId);

    Optional<RouteStop> findByOrderId(UUID orderId);

    List<RouteStop> findByRouteIdAndStopStatus(UUID routeId, StopStatus stopStatus);

    List<RouteStop> findByRouteIdAndLocked(UUID routeId, boolean locked);
}
