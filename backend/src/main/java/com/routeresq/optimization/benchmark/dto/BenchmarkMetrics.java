package com.routeresq.optimization.benchmark.dto;

public class BenchmarkMetrics {

    private double distanceKm;
    private int durationMinutes;
    private int travelDurationMinutes;
    private int serviceDurationMinutes;
    private int vehiclesUsed;
    private int routesCount;
    private int ordersAssigned;
    private int ordersUnassigned;
    private int lateDeliveries;
    private int capacityViolations;
    private int shiftViolations;
    private boolean feasible;
    private long solveTimeMs;

    public BenchmarkMetrics() {
    }

    public BenchmarkMetrics(double distanceKm, int durationMinutes, int travelDurationMinutes, int serviceDurationMinutes, int vehiclesUsed, int routesCount, int ordersAssigned, int ordersUnassigned, int lateDeliveries, int capacityViolations, int shiftViolations, boolean feasible, long solveTimeMs) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.travelDurationMinutes = travelDurationMinutes;
        this.serviceDurationMinutes = serviceDurationMinutes;
        this.vehiclesUsed = vehiclesUsed;
        this.routesCount = routesCount;
        this.ordersAssigned = ordersAssigned;
        this.ordersUnassigned = ordersUnassigned;
        this.lateDeliveries = lateDeliveries;
        this.capacityViolations = capacityViolations;
        this.shiftViolations = shiftViolations;
        this.feasible = feasible;
        this.solveTimeMs = solveTimeMs;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTravelDurationMinutes() {
        return travelDurationMinutes;
    }

    public void setTravelDurationMinutes(int travelDurationMinutes) {
        this.travelDurationMinutes = travelDurationMinutes;
    }

    public int getServiceDurationMinutes() {
        return serviceDurationMinutes;
    }

    public void setServiceDurationMinutes(int serviceDurationMinutes) {
        this.serviceDurationMinutes = serviceDurationMinutes;
    }

    public int getVehiclesUsed() {
        return vehiclesUsed;
    }

    public void setVehiclesUsed(int vehiclesUsed) {
        this.vehiclesUsed = vehiclesUsed;
    }

    public int getRoutesCount() {
        return routesCount;
    }

    public void setRoutesCount(int routesCount) {
        this.routesCount = routesCount;
    }

    public int getOrdersAssigned() {
        return ordersAssigned;
    }

    public void setOrdersAssigned(int ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }

    public int getOrdersUnassigned() {
        return ordersUnassigned;
    }

    public void setOrdersUnassigned(int ordersUnassigned) {
        this.ordersUnassigned = ordersUnassigned;
    }

    public int getLateDeliveries() {
        return lateDeliveries;
    }

    public void setLateDeliveries(int lateDeliveries) {
        this.lateDeliveries = lateDeliveries;
    }

    public int getCapacityViolations() {
        return capacityViolations;
    }

    public void setCapacityViolations(int capacityViolations) {
        this.capacityViolations = capacityViolations;
    }

    public int getShiftViolations() {
        return shiftViolations;
    }

    public void setShiftViolations(int shiftViolations) {
        this.shiftViolations = shiftViolations;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public long getSolveTimeMs() {
        return solveTimeMs;
    }

    public void setSolveTimeMs(long solveTimeMs) {
        this.solveTimeMs = solveTimeMs;
    }
}
