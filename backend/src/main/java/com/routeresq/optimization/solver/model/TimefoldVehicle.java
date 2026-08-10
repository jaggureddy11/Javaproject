package com.routeresq.optimization.solver.model;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.UUID;

public class TimefoldVehicle implements Standstill {

    @PlanningId
    private UUID id;
    private String vehicleCode;
    private UUID depotId;
    private Point depotLocation;
    private BigDecimal maxWeightKg;
    private Integer shiftStartMinutes;
    private Integer shiftEndMinutes;
    private UUID driverId;
    private String driverName;

    private TimefoldCustomer nextCustomer;

    public TimefoldVehicle() {
    }

    public TimefoldVehicle(UUID id, String vehicleCode, UUID depotId, Point depotLocation, BigDecimal maxWeightKg, Integer shiftStartMinutes, Integer shiftEndMinutes, UUID driverId, String driverName) {
        this.id = id;
        this.vehicleCode = vehicleCode;
        this.depotId = depotId;
        this.depotLocation = depotLocation;
        this.maxWeightKg = maxWeightKg;
        this.shiftStartMinutes = shiftStartMinutes != null ? shiftStartMinutes : 480;
        this.shiftEndMinutes = shiftEndMinutes != null ? shiftEndMinutes : 1020;
        this.driverId = driverId;
        this.driverName = driverName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public UUID getDepotId() {
        return depotId;
    }

    public void setDepotId(UUID depotId) {
        this.depotId = depotId;
    }

    public Point getDepotLocation() {
        return depotLocation;
    }

    public void setDepotLocation(Point depotLocation) {
        this.depotLocation = depotLocation;
    }

    public BigDecimal getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(BigDecimal maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public Integer getShiftStartMinutes() {
        return shiftStartMinutes;
    }

    public void setShiftStartMinutes(Integer shiftStartMinutes) {
        this.shiftStartMinutes = shiftStartMinutes;
    }

    public Integer getShiftEndMinutes() {
        return shiftEndMinutes;
    }

    public void setShiftEndMinutes(Integer shiftEndMinutes) {
        this.shiftEndMinutes = shiftEndMinutes;
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

    @Override
    public Point getLocation() {
        return depotLocation;
    }

    @Override
    public TimefoldVehicle getVehicle() {
        return this;
    }

    @Override
    public TimefoldCustomer getNextCustomer() {
        return nextCustomer;
    }

    @Override
    public void setNextCustomer(TimefoldCustomer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }
}
