package com.routeresq.optimization.baseline.model;

import java.util.ArrayList;
import java.util.List;

public class BaselineRouteResult {

    private List<BaselineRoute> routes = new ArrayList<>();
    private double totalDistanceKm;
    private int totalDurationMinutes;
    private int vehiclesUsed;
    private int ordersAssigned;
    private int ordersUnassigned;
    private int lateDeliveries;
    private int capacityViolations;
    private int shiftViolations;
    private boolean feasible;
    private long executionTimeMs;

    public BaselineRouteResult() {
    }

    public List<BaselineRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<BaselineRoute> routes) {
        this.routes = routes;
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

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
