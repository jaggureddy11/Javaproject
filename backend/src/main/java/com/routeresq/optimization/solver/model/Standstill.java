package com.routeresq.optimization.solver.model;

import org.locationtech.jts.geom.Point;

public interface Standstill {

    Point getLocation();

    TimefoldVehicle getVehicle();

    TimefoldCustomer getNextCustomer();

    void setNextCustomer(TimefoldCustomer nextCustomer);
}
