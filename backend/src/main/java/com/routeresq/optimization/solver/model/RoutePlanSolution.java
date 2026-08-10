package com.routeresq.optimization.solver.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.routeresq.routing.matrix.DistanceMatrix;

import java.util.ArrayList;
import java.util.List;

@PlanningSolution
public class RoutePlanSolution {

    @ValueRangeProvider(id = "vehicleRange")
    @ProblemFactCollectionProperty
    private List<TimefoldVehicle> vehicleList = new ArrayList<>();

    @ValueRangeProvider(id = "customerRange")
    @PlanningEntityCollectionProperty
    private List<TimefoldCustomer> customerList = new ArrayList<>();

    @ProblemFactProperty
    private DistanceMatrix distanceMatrix;

    @PlanningScore
    private HardSoftScore score;

    public RoutePlanSolution() {
    }

    public RoutePlanSolution(List<TimefoldVehicle> vehicleList, List<TimefoldCustomer> customerList, DistanceMatrix distanceMatrix) {
        this.vehicleList = vehicleList;
        this.customerList = customerList;
        this.distanceMatrix = distanceMatrix;
    }

    public List<TimefoldVehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<TimefoldVehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    public List<TimefoldCustomer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<TimefoldCustomer> customerList) {
        this.customerList = customerList;
    }

    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    public void setDistanceMatrix(DistanceMatrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
