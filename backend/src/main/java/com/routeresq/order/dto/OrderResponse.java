package com.routeresq.order.dto;

import com.routeresq.order.model.OrderStatus;
import com.routeresq.shared.dto.LocationDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private UUID depotId;
    private String depotName;
    private String customerName;
    private LocationDto location;
    private String addressText;
    private BigDecimal weightKg;
    private BigDecimal volumeM3;
    private Integer windowStartMinutes;
    private Integer windowEndMinutes;
    private Integer serviceDurationMinutes;
    private Integer priority;
    private OrderStatus status;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(UUID id, String orderNumber, UUID depotId, String depotName, String customerName, LocationDto location, String addressText, BigDecimal weightKg, BigDecimal volumeM3, Integer windowStartMinutes, Integer windowEndMinutes, Integer serviceDurationMinutes, Integer priority, OrderStatus status, Integer version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.depotId = depotId;
        this.depotName = depotName;
        this.customerName = customerName;
        this.location = location;
        this.addressText = addressText;
        this.weightKg = weightKg;
        this.volumeM3 = volumeM3;
        this.windowStartMinutes = windowStartMinutes;
        this.windowEndMinutes = windowEndMinutes;
        this.serviceDurationMinutes = serviceDurationMinutes;
        this.priority = priority;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getDepotId() {
        return depotId;
    }

    public void setDepotId(UUID depotId) {
        this.depotId = depotId;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getVolumeM3() {
        return volumeM3;
    }

    public void setVolumeM3(BigDecimal volumeM3) {
        this.volumeM3 = volumeM3;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
