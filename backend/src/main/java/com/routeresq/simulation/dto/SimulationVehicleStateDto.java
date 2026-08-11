package com.routeresq.simulation.dto;

import com.routeresq.simulation.model.SimVehicleStatus;

import java.util.UUID;

public class SimulationVehicleStateDto {

    private UUID vehicleId;
    private String vehicleCode;
    private String driverName;
    private UUID routeId;
    private SimVehicleStatus status;
    private double latitude;
    private double longitude;
    private int currentStopIndex;
    private int totalStops;
    private UUID currentOrderId;
    private String currentOrderNumber;
    private String currentCustomerName;
    private Double distanceTravelledKm;
    private Double distanceRemainingKm;
    private Integer estimatedArrivalMinutes;

    public SimulationVehicleStateDto() {
    }

    public SimulationVehicleStateDto(UUID vehicleId, String vehicleCode, String driverName, UUID routeId, SimVehicleStatus status, double latitude, double longitude, int currentStopIndex, int totalStops, UUID currentOrderId, String currentOrderNumber, String currentCustomerName, Double distanceTravelledKm, Double distanceRemainingKm, Integer estimatedArrivalMinutes) {
        this.vehicleId = vehicleId;
        this.vehicleCode = vehicleCode;
        this.driverName = driverName;
        this.routeId = routeId;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentStopIndex = currentStopIndex;
        this.totalStops = totalStops;
        this.currentOrderId = currentOrderId;
        this.currentOrderNumber = currentOrderNumber;
        this.currentCustomerName = currentCustomerName;
        this.distanceTravelledKm = distanceTravelledKm;
        this.distanceRemainingKm = distanceRemainingKm;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public UUID getRouteId() {
        return routeId;
    }

    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public SimVehicleStatus getStatus() {
        return status;
    }

    public void setStatus(SimVehicleStatus status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCurrentStopIndex() {
        return currentStopIndex;
    }

    public void setCurrentStopIndex(int currentStopIndex) {
        this.currentStopIndex = currentStopIndex;
    }

    public int getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(int totalStops) {
        this.totalStops = totalStops;
    }

    public UUID getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(UUID currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public String getCurrentOrderNumber() {
        return currentOrderNumber;
    }

    public void setCurrentOrderNumber(String currentOrderNumber) {
        this.currentOrderNumber = currentOrderNumber;
    }

    public String getCurrentCustomerName() {
        return currentCustomerName;
    }

    public void setCurrentCustomerName(String currentCustomerName) {
        this.currentCustomerName = currentCustomerName;
    }

    public Double getDistanceTravelledKm() {
        return distanceTravelledKm;
    }

    public void setDistanceTravelledKm(Double distanceTravelledKm) {
        this.distanceTravelledKm = distanceTravelledKm;
    }

    public Double getDistanceRemainingKm() {
        return distanceRemainingKm;
    }

    public void setDistanceRemainingKm(Double distanceRemainingKm) {
        this.distanceRemainingKm = distanceRemainingKm;
    }

    public Integer getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }
}
