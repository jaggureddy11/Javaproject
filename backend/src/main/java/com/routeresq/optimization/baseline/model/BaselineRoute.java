package com.routeresq.optimization.baseline.model;

import com.routeresq.fleet.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class BaselineRoute {

    private Vehicle vehicle;
    private List<BaselineStop> stops = new ArrayList<>();
    private int totalDistanceMeters;
    private int totalDurationMinutes;

    public BaselineRoute() {
    }

    public BaselineRoute(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<BaselineStop> getStops() {
        return stops;
    }

    public void setStops(List<BaselineStop> stops) {
        this.stops = stops;
    }

    public int getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public void setTotalDistanceMeters(int totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public void addStop(BaselineStop stop) {
        this.stops.add(stop);
    }
}
