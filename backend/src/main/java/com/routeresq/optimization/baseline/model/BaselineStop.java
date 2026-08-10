package com.routeresq.optimization.baseline.model;

import com.routeresq.order.model.Order;

import java.util.UUID;

public class BaselineStop {

    private UUID stopId;
    private Order order;
    private int sequenceNumber;
    private int estimatedArrivalMinutes;
    private int estimatedDepartureMinutes;
    private boolean late;

    public BaselineStop() {
    }

    public BaselineStop(UUID stopId, Order order, int sequenceNumber, int estimatedArrivalMinutes, int estimatedDepartureMinutes, boolean late) {
        this.stopId = stopId;
        this.order = order;
        this.sequenceNumber = sequenceNumber;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
        this.late = late;
    }

    public UUID getStopId() {
        return stopId;
    }

    public void setStopId(UUID stopId) {
        this.stopId = stopId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(int estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public int getEstimatedDepartureMinutes() {
        return estimatedDepartureMinutes;
    }

    public void setEstimatedDepartureMinutes(int estimatedDepartureMinutes) {
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
    }

    public boolean isLate() {
        return late;
    }

    public void setLate(boolean late) {
        this.late = late;
    }
}
