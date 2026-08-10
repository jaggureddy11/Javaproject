package com.routeresq.order.model;

import com.routeresq.fleet.model.Depot;
import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @NotNull(message = "Origin depot is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @NotBlank(message = "Order number is required")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @NotNull(message = "Delivery location is required")
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @NotBlank(message = "Address text is required")
    @Column(name = "address_text", nullable = false, columnDefinition = "TEXT")
    private String addressText;

    @NotNull(message = "Order weight is required")
    @DecimalMin(value = "0.01", message = "Order weight must be greater than 0")
    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @DecimalMin(value = "0.01", message = "Order volume must be greater than 0")
    @Column(name = "volume_m3", precision = 10, scale = 2)
    private BigDecimal volumeM3 = new BigDecimal("0.10");

    @Min(value = 0, message = "Window start minutes must be >= 0")
    @Max(value = 1440, message = "Window start minutes must be <= 1440")
    @Column(name = "window_start_minutes", nullable = false)
    private Integer windowStartMinutes;

    @Min(value = 0, message = "Window end minutes must be >= 0")
    @Max(value = 1440, message = "Window end minutes must be <= 1440")
    @Column(name = "window_end_minutes", nullable = false)
    private Integer windowEndMinutes;

    @Min(value = 1, message = "Service duration must be at least 1 minute")
    @Column(name = "service_duration_minutes")
    private Integer serviceDurationMinutes = 10;

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

    @NotNull(message = "Order status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.UNASSIGNED;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Order() {
    }

    public Order(Depot depot, String orderNumber, String customerName, Point location, String addressText, BigDecimal weightKg, BigDecimal volumeM3, Integer windowStartMinutes, Integer windowEndMinutes, Integer serviceDurationMinutes, Integer priority, OrderStatus status, Integer version) {
        this.depot = depot;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.location = location;
        this.addressText = addressText;
        this.weightKg = weightKg;
        if (volumeM3 != null) this.volumeM3 = volumeM3;
        this.windowStartMinutes = windowStartMinutes;
        this.windowEndMinutes = windowEndMinutes;
        if (serviceDurationMinutes != null) this.serviceDurationMinutes = serviceDurationMinutes;
        if (priority != null) this.priority = priority;
        if (status != null) this.status = status;
        if (version != null) this.version = version;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
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

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
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

    public void validateTimeWindow() {
        if (windowStartMinutes != null && windowEndMinutes != null && windowStartMinutes >= windowEndMinutes) {
            throw new IllegalArgumentException("Delivery window start minutes (" + windowStartMinutes + 
                    ") must be strictly less than window end minutes (" + windowEndMinutes + ")");
        }
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public static class OrderBuilder {
        private Depot depot;
        private String orderNumber;
        private String customerName;
        private Point location;
        private String addressText;
        private BigDecimal weightKg;
        private BigDecimal volumeM3 = new BigDecimal("0.10");
        private Integer windowStartMinutes;
        private Integer windowEndMinutes;
        private Integer serviceDurationMinutes = 10;
        private Integer priority = 1;
        private OrderStatus status = OrderStatus.UNASSIGNED;
        private Integer version = 0;

        public OrderBuilder depot(Depot depot) {
            this.depot = depot;
            return this;
        }

        public OrderBuilder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public OrderBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public OrderBuilder location(Point location) {
            this.location = location;
            return this;
        }

        public OrderBuilder addressText(String addressText) {
            this.addressText = addressText;
            return this;
        }

        public OrderBuilder weightKg(BigDecimal weightKg) {
            this.weightKg = weightKg;
            return this;
        }

        public OrderBuilder volumeM3(BigDecimal volumeM3) {
            this.volumeM3 = volumeM3;
            return this;
        }

        public OrderBuilder windowStartMinutes(Integer windowStartMinutes) {
            this.windowStartMinutes = windowStartMinutes;
            return this;
        }

        public OrderBuilder windowEndMinutes(Integer windowEndMinutes) {
            this.windowEndMinutes = windowEndMinutes;
            return this;
        }

        public OrderBuilder serviceDurationMinutes(Integer serviceDurationMinutes) {
            this.serviceDurationMinutes = serviceDurationMinutes;
            return this;
        }

        public OrderBuilder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public OrderBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public Order build() {
            return new Order(depot, orderNumber, customerName, location, addressText, weightKg, volumeM3, windowStartMinutes, windowEndMinutes, serviceDurationMinutes, priority, status, version);
        }
    }
}
