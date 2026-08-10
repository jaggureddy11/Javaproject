package com.routeresq.fleet.model;

import com.routeresq.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "depots")
public class Depot extends BaseEntity {

    @NotBlank(message = "Depot name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Depot location coordinates are required")
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @NotBlank(message = "Address text is required")
    @Column(name = "address_text", nullable = false, columnDefinition = "TEXT")
    private String addressText;

    public Depot() {
    }

    public Depot(String name, Point location, String addressText) {
        this.name = name;
        this.location = location;
        this.addressText = addressText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public static DepotBuilder builder() {
        return new DepotBuilder();
    }

    public static class DepotBuilder {
        private String name;
        private Point location;
        private String addressText;

        public DepotBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DepotBuilder location(Point location) {
            this.location = location;
            return this;
        }

        public DepotBuilder addressText(String addressText) {
            this.addressText = addressText;
            return this;
        }

        public Depot build() {
            return new Depot(name, location, addressText);
        }
    }
}
