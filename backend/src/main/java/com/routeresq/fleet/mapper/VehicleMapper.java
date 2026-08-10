package com.routeresq.fleet.mapper;

import com.routeresq.fleet.dto.VehicleRequest;
import com.routeresq.fleet.dto.VehicleResponse;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.Vehicle;
import com.routeresq.shared.dto.LocationDto;
import com.routeresq.shared.util.GeometryUtils;

public class VehicleMapper {

    public static Vehicle toEntity(VehicleRequest request, Depot depot, Driver driver) {
        Vehicle vehicle = Vehicle.builder()
                .vehicleCode(request.getVehicleCode())
                .depot(depot)
                .driver(driver)
                .maxWeightKg(request.getMaxWeightKg())
                .maxVolumeM3(request.getMaxVolumeM3())
                .status(request.getStatus())
                .build();

        if (request.getCurrentLocation() != null) {
            vehicle.setCurrentLocation(GeometryUtils.createPoint(
                    request.getCurrentLocation().getLatitude(),
                    request.getCurrentLocation().getLongitude()
            ));
        } else if (depot != null) {
            vehicle.setCurrentLocation(depot.getLocation());
        }

        return vehicle;
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        LocationDto location = null;
        if (vehicle.getCurrentLocation() != null) {
            location = new LocationDto(
                    GeometryUtils.getLatitude(vehicle.getCurrentLocation()),
                    GeometryUtils.getLongitude(vehicle.getCurrentLocation())
            );
        }

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleCode(),
                vehicle.getDepot() != null ? vehicle.getDepot().getId() : null,
                vehicle.getDepot() != null ? vehicle.getDepot().getName() : null,
                vehicle.getDriver() != null ? vehicle.getDriver().getId() : null,
                vehicle.getDriver() != null ? vehicle.getDriver().getName() : null,
                vehicle.getMaxWeightKg(),
                vehicle.getMaxVolumeM3(),
                vehicle.getStatus(),
                location,
                vehicle.getVersion(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
