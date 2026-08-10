package com.routeresq.optimization.solver.model;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import com.routeresq.optimization.solver.comparator.CustomerDifficultyComparator;
import com.routeresq.routing.matrix.DistanceMatrix;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.UUID;

@PlanningEntity(difficultyComparatorClass = CustomerDifficultyComparator.class)
public class TimefoldCustomer implements Standstill {

    @PlanningId
    private UUID id;
    private String orderNumber;
    private String customerName;
    private Point location;
    private BigDecimal weightKg;
    private Integer windowStartMinutes;
    private Integer windowEndMinutes;
    private Integer serviceDurationMinutes = 10;
    private Integer priority = 1;

    @PlanningVariable(
            valueRangeProviderRefs = {"vehicleRange", "customerRange"},
            graphType = PlanningVariableGraphType.CHAINED
    )
    private Standstill previousStandstill;

    private TimefoldCustomer nextCustomer;

    public TimefoldCustomer() {
    }

    public TimefoldCustomer(UUID id, String orderNumber, String customerName, Point location, BigDecimal weightKg, Integer windowStartMinutes, Integer windowEndMinutes, Integer serviceDurationMinutes, Integer priority) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.location = location;
        this.weightKg = weightKg;
        this.windowStartMinutes = windowStartMinutes != null ? windowStartMinutes : 540;
        this.windowEndMinutes = windowEndMinutes != null ? windowEndMinutes : 1020;
        this.serviceDurationMinutes = serviceDurationMinutes != null ? serviceDurationMinutes : 10;
        this.priority = priority != null ? priority : 1;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getWindowStartMinutes() {
        return windowStartMinutes;
    }

    public void setWindowStartMinutes(Integer windowStartMinutes) {
        this.windowStartMinutes = windowStartMinutes;
    }

    public Integer getWindowEndMinutes() {
        return windowEndMinutes;
    }

    public void setWindowEndMinutes(Integer windowEndMinutes) {
        this.windowEndMinutes = windowEndMinutes;
    }

    public Integer getServiceDurationMinutes() {
        return serviceDurationMinutes;
    }

    public void setServiceDurationMinutes(Integer serviceDurationMinutes) {
        this.serviceDurationMinutes = serviceDurationMinutes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @Override
    public TimefoldVehicle getVehicle() {
        if (previousStandstill == null) {
            return null;
        }
        return previousStandstill.getVehicle();
    }

    @Override
    public TimefoldCustomer getNextCustomer() {
        return nextCustomer;
    }

    @Override
    public void setNextCustomer(TimefoldCustomer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    public Integer getArrivalTimeMinutes(DistanceMatrix distanceMatrix) {
        if (previousStandstill == null) {
            return null;
        }
        if (previousStandstill instanceof TimefoldVehicle v) {
            int travelTime = distanceMatrix != null ? distanceMatrix.getTravelTimeMinutes(v.getDepotLocation(), location) : 0;
            return Math.max(windowStartMinutes, v.getShiftStartMinutes() + travelTime);
        }
        if (previousStandstill instanceof TimefoldCustomer prev) {
            Integer prevArrival = prev.getArrivalTimeMinutes(distanceMatrix);
            if (prevArrival == null) return null;
            int departure = prevArrival + prev.getServiceDurationMinutes();
            int travelTime = distanceMatrix != null ? distanceMatrix.getTravelTimeMinutes(prev.getLocation(), location) : 0;
            return Math.max(windowStartMinutes, departure + travelTime);
        }
        return null;
    }
}
