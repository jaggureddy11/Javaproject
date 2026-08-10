package com.routeresq.routing.provider;

import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class HaversineRoutingProvider implements RoutingProvider {

    private static final double AVERAGE_SPEED_KMH = 30.0; // 30 km/h urban speed

    @Override
    public double getDistanceMeters(Point from, Point to) {
        if (from == null || to == null || from.equalsExact(to)) {
            return 0.0;
        }
        return GeometryUtils.haversineMeters(from, to);
    }

    @Override
    public int getTravelTimeMinutes(Point from, Point to) {
        double meters = getDistanceMeters(from, to);
        if (meters == 0.0) {
            return 0;
        }
        double km = meters / 1000.0;
        double hours = km / AVERAGE_SPEED_KMH;
        return (int) Math.ceil(hours * 60.0);
    }
}
