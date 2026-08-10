package com.routeresq.routing.matrix;

import com.routeresq.routing.provider.RoutingProvider;
import org.locationtech.jts.geom.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceMatrix {

    private final Map<String, Double> distanceCache = new HashMap<>();
    private final Map<String, Integer> durationCache = new HashMap<>();

    public DistanceMatrix() {
    }

    public static DistanceMatrix build(List<Point> locations, RoutingProvider provider) {
        DistanceMatrix matrix = new DistanceMatrix();
        for (Point from : locations) {
            for (Point to : locations) {
                String key = getKey(from, to);
                double distance = provider.getDistanceMeters(from, to);
                int duration = provider.getTravelTimeMinutes(from, to);
                matrix.distanceCache.put(key, distance);
                matrix.durationCache.put(key, duration);
            }
        }
        return matrix;
    }

    public double getDistanceMeters(Point from, Point to) {
        if (from == null || to == null || from.equalsExact(to)) {
            return 0.0;
        }
        String key = getKey(from, to);
        return distanceCache.getOrDefault(key, 0.0);
    }

    public int getTravelTimeMinutes(Point from, Point to) {
        if (from == null || to == null || from.equalsExact(to)) {
            return 0;
        }
        String key = getKey(from, to);
        return durationCache.getOrDefault(key, 0);
    }

    private static String getKey(Point from, Point to) {
        return from.getX() + "," + from.getY() + "->" + to.getX() + "," + to.getY();
    }
}
