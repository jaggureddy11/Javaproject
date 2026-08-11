package com.routeresq.incident.dto;

import com.routeresq.incident.model.IncidentStatus;

import java.util.List;
import java.util.UUID;

public class RecoveryPlanResponse {

    private UUID incidentId;
    private IncidentStatus status;
    private UUID originalRouteId;
    private List<UUID> replacementRouteIds;
    private List<String> replacementVehicleCodes;
    private int affectedOrdersCount;
    private int reassignedOrdersCount;
    private int solveTimeMs;
    private double totalDistanceChangeKm;
    private boolean feasible;
    private String message;

    public RecoveryPlanResponse() {
    }

    public RecoveryPlanResponse(UUID incidentId, IncidentStatus status, UUID originalRouteId, List<UUID> replacementRouteIds, List<String> replacementVehicleCodes, int affectedOrdersCount, int reassignedOrdersCount, int solveTimeMs, double totalDistanceChangeKm, boolean feasible, String message) {
        this.incidentId = incidentId;
        this.status = status;
        this.originalRouteId = originalRouteId;
        this.replacementRouteIds = replacementRouteIds;
        this.replacementVehicleCodes = replacementVehicleCodes;
        this.affectedOrdersCount = affectedOrdersCount;
        this.reassignedOrdersCount = reassignedOrdersCount;
        this.solveTimeMs = solveTimeMs;
        this.totalDistanceChangeKm = totalDistanceChangeKm;
        this.feasible = feasible;
        this.message = message;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public UUID getOriginalRouteId() {
        return originalRouteId;
    }

    public void setOriginalRouteId(UUID originalRouteId) {
        this.originalRouteId = originalRouteId;
    }

    public List<UUID> getReplacementRouteIds() {
        return replacementRouteIds;
    }

    public void setReplacementRouteIds(List<UUID> replacementRouteIds) {
        this.replacementRouteIds = replacementRouteIds;
    }

    public List<String> getReplacementVehicleCodes() {
        return replacementVehicleCodes;
    }

    public void setReplacementVehicleCodes(List<String> replacementVehicleCodes) {
        this.replacementVehicleCodes = replacementVehicleCodes;
    }

    public int getAffectedOrdersCount() {
        return affectedOrdersCount;
    }

    public void setAffectedOrdersCount(int affectedOrdersCount) {
        this.affectedOrdersCount = affectedOrdersCount;
    }

    public int getReassignedOrdersCount() {
        return reassignedOrdersCount;
    }

    public void setReassignedOrdersCount(int reassignedOrdersCount) {
        this.reassignedOrdersCount = reassignedOrdersCount;
    }

    public int getSolveTimeMs() {
        return solveTimeMs;
    }

    public void setSolveTimeMs(int solveTimeMs) {
        this.solveTimeMs = solveTimeMs;
    }

    public double getTotalDistanceChangeKm() {
        return totalDistanceChangeKm;
    }

    public void setTotalDistanceChangeKm(double totalDistanceChangeKm) {
        this.totalDistanceChangeKm = totalDistanceChangeKm;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
