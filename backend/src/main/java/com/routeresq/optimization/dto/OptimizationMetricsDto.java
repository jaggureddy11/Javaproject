package com.routeresq.optimization.dto;

public class OptimizationMetricsDto {

    private double totalDistanceKm;
    private int totalDurationMinutes;
    private int vehiclesUsed;
    private int ordersAssigned;
    private int unassignedOrders;

    public OptimizationMetricsDto() {
    }

    public OptimizationMetricsDto(double totalDistanceKm, int totalDurationMinutes, int vehiclesUsed, int ordersAssigned, int unassignedOrders) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalDurationMinutes = totalDurationMinutes;
        this.vehiclesUsed = vehiclesUsed;
        this.ordersAssigned = ordersAssigned;
        this.unassignedOrders = unassignedOrders;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public int getVehiclesUsed() {
        return vehiclesUsed;
    }

    public void setVehiclesUsed(int vehiclesUsed) {
        this.vehiclesUsed = vehiclesUsed;
    }

    public int getOrdersAssigned() {
        return ordersAssigned;
    }

    public void setOrdersAssigned(int ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }

    public int getUnassignedOrders() {
        return unassignedOrders;
    }

    public void setUnassignedOrders(int unassignedOrders) {
        this.unassignedOrders = unassignedOrders;
    }
}
