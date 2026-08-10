package com.routeresq.fleet.model;

import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {

    @NotBlank(message = "License number is required")
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @NotBlank(message = "Driver name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 30)
    private String phone;

    @NotNull(message = "Driver status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DriverStatus status = DriverStatus.ACTIVE;

    @Min(value = 0, message = "Shift start minutes must be >= 0")
    @Max(value = 1440, message = "Shift start minutes must be <= 1440")
    @Column(name = "shift_start_minutes", nullable = false)
    private Integer shiftStartMinutes = 480;

    @Min(value = 0, message = "Shift end minutes must be >= 0")
    @Max(value = 1440, message = "Shift end minutes must be <= 1440")
    @Column(name = "shift_end_minutes", nullable = false)
    private Integer shiftEndMinutes = 1020;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Driver() {
    }

    public Driver(String licenseNumber, String name, String phone, DriverStatus status, Integer shiftStartMinutes, Integer shiftEndMinutes, Integer version) {
        this.licenseNumber = licenseNumber;
        this.name = name;
        this.phone = phone;
        if (status != null) this.status = status;
        if (shiftStartMinutes != null) this.shiftStartMinutes = shiftStartMinutes;
        if (shiftEndMinutes != null) this.shiftEndMinutes = shiftEndMinutes;
        if (version != null) this.version = version;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void validateShift() {
        if (shiftStartMinutes != null && shiftEndMinutes != null && shiftStartMinutes >= shiftEndMinutes) {
            throw new IllegalArgumentException("Driver shift start minutes (" + shiftStartMinutes + 
                    ") must be strictly less than shift end minutes (" + shiftEndMinutes + ")");
        }
    }

    public static DriverBuilder builder() {
        return new DriverBuilder();
    }

    public static class DriverBuilder {
        private String licenseNumber;
        private String name;
        private String phone;
        private DriverStatus status = DriverStatus.ACTIVE;
        private Integer shiftStartMinutes = 480;
        private Integer shiftEndMinutes = 1020;
        private Integer version = 0;

        public DriverBuilder licenseNumber(String licenseNumber) {
            this.licenseNumber = licenseNumber;
            return this;
        }

        public DriverBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DriverBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public DriverBuilder status(DriverStatus status) {
            this.status = status;
            return this;
        }

        public DriverBuilder shiftStartMinutes(Integer shiftStartMinutes) {
            this.shiftStartMinutes = shiftStartMinutes;
            return this;
        }

        public DriverBuilder shiftEndMinutes(Integer shiftEndMinutes) {
            this.shiftEndMinutes = shiftEndMinutes;
            return this;
        }

        public DriverBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public Driver build() {
            return new Driver(licenseNumber, name, phone, status, shiftStartMinutes, shiftEndMinutes, version);
        }
    }
}
