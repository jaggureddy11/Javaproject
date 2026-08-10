package com.routeresq.fleet.service;

import com.routeresq.fleet.dto.VehicleRequest;
import com.routeresq.fleet.dto.VehicleResponse;
import com.routeresq.fleet.mapper.VehicleMapper;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.model.VehicleStatus;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.fleet.repository.DriverRepository;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.shared.dto.PageResponse;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DepotRepository depotRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          DepotRepository depotRepository,
                          DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.depotRepository = depotRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot", request.getDepotId()));

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        }

        Vehicle vehicle = VehicleMapper.toEntity(request, depot, driver);
        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        return VehicleMapper.toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> listVehicles(VehicleStatus status, UUID depotId, Pageable pageable) {
        Page<Vehicle> page;
        if (status != null && depotId != null) {
            page = vehicleRepository.findByStatusAndDepotId(status, depotId, pageable);
        } else if (status != null) {
            page = vehicleRepository.findByStatus(status, pageable);
        } else if (depotId != null) {
            page = vehicleRepository.findByDepotId(depotId, pageable);
        } else {
            page = vehicleRepository.findAll(pageable);
        }
        return PageResponse.fromPage(page, VehicleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getNearbyVehicles(double latitude, double longitude, double radiusMeters) {
        Point point = GeometryUtils.createPoint(latitude, longitude);
        List<Vehicle> nearby = vehicleRepository.findVehiclesWithinRadius(point, radiusMeters);
        return nearby.stream().map(VehicleMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot", request.getDepotId()));

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        }

        vehicle.setVehicleCode(request.getVehicleCode());
        vehicle.setDepot(depot);
        vehicle.setDriver(driver);
        vehicle.setMaxWeightKg(request.getMaxWeightKg());
        vehicle.setMaxVolumeM3(request.getMaxVolumeM3());
        vehicle.setStatus(request.getStatus());

        if (request.getCurrentLocation() != null) {
            vehicle.setCurrentLocation(GeometryUtils.createPoint(
                    request.getCurrentLocation().getLatitude(),
                    request.getCurrentLocation().getLongitude()
            ));
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updated);
    }

    @Transactional
    public void deleteVehicle(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", id);
        }
        vehicleRepository.deleteById(id);
    }
}
