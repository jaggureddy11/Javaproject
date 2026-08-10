package com.routeresq.order.dto;

import com.routeresq.order.model.OrderStatus;
import com.routeresq.shared.dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateOrderRequest {

    private String orderNumber;
    private UUID depotId;
    private String customerName;

    @Valid
    private LocationDto location;

    private String addressText;

    @DecimalMin(value = "0.01", message = "Weight must be > 0")
    private BigDecimal weightKg;

    @DecimalMin(value = "0.01", message = "Volume must be > 0")
    private BigDecimal volumeM3;

    @Min(value = 0, message = "Window start must be >= 0")
    @Max(value = 1440, message = "Window start must be <= 1440")
    private Integer windowStartMinutes;

    @Min(value = 0, message = "Window end must be >= 0")
    @Max(value = 1440, message = "Window end must be <= 1440")
    private Integer windowEndMinutes;

    @Min(value = 1, message = "Service duration must be >= 1 minute")
    private Integer serviceDurationMinutes;

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    private Integer priority;

    private OrderStatus status;

    public UpdateOrderRequest() {
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
}
