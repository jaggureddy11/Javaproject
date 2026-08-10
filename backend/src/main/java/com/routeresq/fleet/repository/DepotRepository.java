package com.routeresq.fleet.repository;

import com.routeresq.fleet.model.Depot;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepotRepository extends JpaRepository<Depot, UUID> {

    Optional<Depot> findByName(String name);

    @Query("SELECT d FROM Depot d WHERE ST_DWithin(d.location, :point, :radiusMeters) = true")
    List<Depot> findDepotsWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);
}
