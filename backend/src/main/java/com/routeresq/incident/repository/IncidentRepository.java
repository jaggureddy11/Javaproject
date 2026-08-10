package com.routeresq.incident.repository;

import com.routeresq.incident.model.Incident;
import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByIncidentType(IncidentType incidentType);

    List<Incident> findByVehicleId(UUID vehicleId);

    List<Incident> findByOrderId(UUID orderId);
}
