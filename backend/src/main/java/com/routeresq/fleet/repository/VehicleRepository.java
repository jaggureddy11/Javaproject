package com.routeresq.fleet.repository;

import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByVehicleCode(String vehicleCode);

    List<Vehicle> findByStatus(VehicleStatus status);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    List<Vehicle> findByDepotId(UUID depotId);

    Page<Vehicle> findByDepotId(UUID depotId, Pageable pageable);

    Page<Vehicle> findByStatusAndDepotId(VehicleStatus status, UUID depotId, Pageable pageable);

    @Query("SELECT v FROM v WHERE ST_DWithin(v.currentLocation, :point, :radiusMeters) = true")
    List<Vehicle> findVehiclesWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);
}
