package com.routeresq.optimization.repository;

import com.routeresq.optimization.model.OptimizationRun;
import com.routeresq.optimization.model.OptimizationRunType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationRunRepository extends JpaRepository<OptimizationRun, UUID> {

    Optional<OptimizationRun> findTopByOrderByCreatedAtDesc();

    List<OptimizationRun> findByRunType(OptimizationRunType runType);
}
