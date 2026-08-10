package com.routeresq.optimization.dto;

import com.routeresq.routing.model.RouteStatus;

import java.util.List;
import java.util.UUID;

public class RouteResultDto {

    private UUID routeId;
    private UUID vehicleId;
    private String vehicleCode;
    private UUID driverId;
    private String driverName;
    private RouteStatus status;
    private Integer totalDistanceMeters;
    private Integer totalDurationMinutes;
    private List<StopResultDto> stops;

    public RouteResultDto() {
    }

    public RouteResultDto(UUID routeId, UUID vehicleId, String vehicleCode, UUID driverId, String driverName, RouteStatus status, Integer totalDistanceMeters, Integer totalDurationMinutes, List<StopResultDto> stops) {
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.vehicleCode = vehicleCode;
        this.driverId = driverId;
        this.driverName = driverName;
        this.status = status;
        this.totalDistanceMeters = totalDistanceMeters;
        this.totalDurationMinutes = totalDurationMinutes;
        this.stops = stops;
    }

    public UUID getRouteId() {
        return routeId;
    }

    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }

    public Integer getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public void setTotalDistanceMeters(Integer totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public List<StopResultDto> getStops() {
        return stops;
    }

    public void setStops(List<StopResultDto> stops) {
        this.stops = stops;
    }
}
