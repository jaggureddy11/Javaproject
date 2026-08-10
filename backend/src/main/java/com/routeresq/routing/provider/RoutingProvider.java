package com.routeresq.routing.provider;

import org.locationtech.jts.geom.Point;

public interface RoutingProvider {

    double getDistanceMeters(Point from, Point to);

    int getTravelTimeMinutes(Point from, Point to);
}
