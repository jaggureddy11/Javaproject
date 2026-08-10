package com.routeresq.fleet.dto;

import com.routeresq.fleet.model.DriverStatus;

import java.time.Instant;
import java.util.UUID;

public class DriverResponse {

    private UUID id;
    private String name;
    private String licenseNumber;
    private String phone;
    private DriverStatus status;
    private Integer shiftStartMinutes;
    private Integer shiftEndMinutes;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    public DriverResponse() {
    }

    public DriverResponse(UUID id, String name, String licenseNumber, String phone, DriverStatus status, Integer shiftStartMinutes, Integer shiftEndMinutes, Integer version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.status = status;
        this.shiftStartMinutes = shiftStartMinutes;
        this.shiftEndMinutes = shiftEndMinutes;
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
