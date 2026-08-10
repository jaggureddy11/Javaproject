package com.routeresq.order.dto;

import com.routeresq.order.model.OrderStatus;
import com.routeresq.shared.dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOrderRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Depot ID is required")
    private UUID depotId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Delivery location is required")
    @Valid
    private LocationDto location;

    @NotBlank(message = "Address is required")
    private String addressText;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be > 0")
    private BigDecimal weightKg;

    @DecimalMin(value = "0.01", message = "Volume must be > 0")
    private BigDecimal volumeM3 = new BigDecimal("0.25");

    @NotNull(message = "Window start minutes is required")
    @Min(value = 0, message = "Window start must be >= 0")
    @Max(value = 1440, message = "Window start must be <= 1440")
    private Integer windowStartMinutes = 540; // 09:00

    @NotNull(message = "Window end minutes is required")
    @Min(value = 0, message = "Window end must be >= 0")
    @Max(value = 1440, message = "Window end must be <= 1440")
    private Integer windowEndMinutes = 1020; // 17:00

    @Min(value = 1, message = "Service duration must be >= 1 minute")
    private Integer serviceDurationMinutes = 10;

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    private Integer priority = 1;

    @NotNull(message = "Order status is required")
    private OrderStatus status = OrderStatus.UNASSIGNED;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String orderNumber, UUID depotId, String customerName, LocationDto location, String addressText, BigDecimal weightKg, BigDecimal volumeM3, Integer windowStartMinutes, Integer windowEndMinutes, Integer serviceDurationMinutes, Integer priority, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.depotId = depotId;
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
