package com.routeresq.fleet.mapper;

import com.routeresq.fleet.dto.DepotRequest;
import com.routeresq.fleet.dto.DepotResponse;
import com.routeresq.fleet.model.Depot;
import com.routeresq.shared.dto.LocationDto;
import com.routeresq.shared.util.GeometryUtils;

public class DepotMapper {

    public static Depot toEntity(DepotRequest request) {
        return Depot.builder()
                .name(request.getName())
                .addressText(request.getAddressText())
                .location(GeometryUtils.createPoint(request.getLocation().getLatitude(), request.getLocation().getLongitude()))
                .build();
    }

    public static DepotResponse toResponse(Depot depot) {
        LocationDto location = new LocationDto(
                GeometryUtils.getLatitude(depot.getLocation()),
                GeometryUtils.getLongitude(depot.getLocation())
        );
        return new DepotResponse(
                depot.getId(),
                depot.getName(),
                depot.getAddressText(),
                location,
                depot.getCreatedAt(),
                depot.getUpdatedAt()
        );
    }
}
