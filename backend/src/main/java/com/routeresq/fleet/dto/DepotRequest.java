package com.routeresq.fleet.dto;

import com.routeresq.shared.dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DepotRequest {

    @NotBlank(message = "Depot name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String addressText;

    @NotNull(message = "Location coordinates are required")
    @Valid
    private LocationDto location;

    public DepotRequest() {
    }

    public DepotRequest(String name, String addressText, LocationDto location) {
        this.name = name;
        this.addressText = addressText;
        this.location = location;
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
}
