package com.routeresq.fleet.dto;

import com.routeresq.fleet.model.DriverStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DriverRequest {

    @NotBlank(message = "Driver name is required")
    private String name;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String phone;

    @NotNull(message = "Driver status is required")
    private DriverStatus status = DriverStatus.ACTIVE;

    @NotNull(message = "Shift start minutes is required")
    @Min(value = 0, message = "Shift start minutes must be >= 0")
    @Max(value = 1440, message = "Shift start minutes must be <= 1440")
    private Integer shiftStartMinutes = 480; // 08:00

    @NotNull(message = "Shift end minutes is required")
    @Min(value = 0, message = "Shift end minutes must be >= 0")
    @Max(value = 1440, message = "Shift end minutes must be <= 1440")
    private Integer shiftEndMinutes = 1020; // 17:00

    public DriverRequest() {
    }

    public DriverRequest(String name, String licenseNumber, String phone, DriverStatus status, Integer shiftStartMinutes, Integer shiftEndMinutes) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.status = status;
        this.shiftStartMinutes = shiftStartMinutes;
        this.shiftEndMinutes = shiftEndMinutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
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
}
