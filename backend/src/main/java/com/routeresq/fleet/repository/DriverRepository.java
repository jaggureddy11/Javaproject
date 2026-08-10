package com.routeresq.fleet.repository;

import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(DriverStatus status);

    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);
}
