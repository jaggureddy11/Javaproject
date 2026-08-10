package com.routeresq.fleet.dto;

import com.routeresq.shared.dto.LocationDto;

import java.time.Instant;
import java.util.UUID;

public class DepotResponse {

    private UUID id;
    private String name;
    private String addressText;
    private LocationDto location;
    private Instant createdAt;
    private Instant updatedAt;

    public DepotResponse() {
    }

    public DepotResponse(UUID id, String name, String addressText, LocationDto location, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.addressText = addressText;
        this.location = location;
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

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
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
