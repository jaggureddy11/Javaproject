package com.routeresq.fleet.mapper;

import com.routeresq.fleet.dto.DriverRequest;
import com.routeresq.fleet.dto.DriverResponse;
import com.routeresq.fleet.model.Driver;

public class DriverMapper {

    public static Driver toEntity(DriverRequest request) {
        return Driver.builder()
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber())
                .phone(request.getPhone())
                .status(request.getStatus())
                .shiftStartMinutes(request.getShiftStartMinutes())
                .shiftEndMinutes(request.getShiftEndMinutes())
                .build();
    }

    public static DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getLicenseNumber(),
                driver.getPhone(),
                driver.getStatus(),
                driver.getShiftStartMinutes(),
                driver.getShiftEndMinutes(),
                driver.getVersion(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}
