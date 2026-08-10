package com.routeresq.shared.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeometryUtils {

    public static final int SRID_WGS84 = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private GeometryUtils() {
        // Utility class
    }

    /**
     * Creates a JTS Point from latitude and longitude with WGS 84 SRID 4326.
     * Note: JTS Coordinate order is (x, y) = (longitude, latitude).
     */
    public static Point createPoint(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees. Got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees. Got: " + longitude);
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID_WGS84);
        return point;
    }

    public static double getLatitude(Point point) {
        return point != null ? point.getY() : 0.0;
    }

    public static double getLongitude(Point point) {
        return point != null ? point.getX() : 0.0;
    }

    /**
     * Calculates geodesic distance in meters between two lat/lon coordinates using the Haversine formula.
     */
    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double haversineMeters(Point p1, Point p2) {
        if (p1 == null || p2 == null) return 0.0;
        return haversineMeters(p1.getY(), p1.getX(), p2.getY(), p2.getX());
    }
}
