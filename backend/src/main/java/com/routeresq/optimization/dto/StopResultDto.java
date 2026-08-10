package com.routeresq.optimization.dto;

import java.util.UUID;

public class StopResultDto {

    private UUID stopId;
    private UUID orderId;
    private String orderNumber;
    private String customerName;
    private int sequenceNumber;
    private Integer estimatedArrivalMinutes;
    private Integer estimatedDepartureMinutes;

    public StopResultDto() {
    }

    public StopResultDto(UUID stopId, UUID orderId, String orderNumber, String customerName, int sequenceNumber, Integer estimatedArrivalMinutes, Integer estimatedDepartureMinutes) {
        this.stopId = stopId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.sequenceNumber = sequenceNumber;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
    }

    public UUID getStopId() {
        return stopId;
    }

    public void setStopId(UUID stopId) {
        this.stopId = stopId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public Integer getEstimatedDepartureMinutes() {
        return estimatedDepartureMinutes;
    }

    public void setEstimatedDepartureMinutes(Integer estimatedDepartureMinutes) {
        this.estimatedDepartureMinutes = estimatedDepartureMinutes;
    }
}
