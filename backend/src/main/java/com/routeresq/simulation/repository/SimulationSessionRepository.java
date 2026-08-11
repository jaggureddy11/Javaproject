package com.routeresq.simulation.repository;

import com.routeresq.simulation.model.SimulationSession;
import com.routeresq.simulation.model.SimulationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimulationSessionRepository extends JpaRepository<SimulationSession, UUID> {

    Optional<SimulationSession> findByOptimizationRunIdAndStatus(UUID optimizationRunId, SimulationStatus status);

    List<SimulationSession> findByOptimizationRunId(UUID optimizationRunId);
}
