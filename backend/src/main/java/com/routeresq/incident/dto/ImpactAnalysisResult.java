package com.routeresq.incident.dto;

import java.util.List;
import java.util.UUID;

public class ImpactAnalysisResult {

    private UUID incidentId;
    private UUID brokenVehicleId;
    private String brokenVehicleCode;
    private UUID affectedRouteId;
    private int completedStopsCount;
    private int affectedOrdersCount;
    private List<UUID> affectedOrderIds;
    private List<String> affectedOrderNumbers;
    private List<UUID> candidateVehicleIds;
    private List<String> candidateVehicleCodes;
    private boolean recoveryFeasible;
    private String message;

    public ImpactAnalysisResult() {
    }

    public ImpactAnalysisResult(UUID incidentId, UUID brokenVehicleId, String brokenVehicleCode, UUID affectedRouteId, int completedStopsCount, int affectedOrdersCount, List<UUID> affectedOrderIds, List<String> affectedOrderNumbers, List<UUID> candidateVehicleIds, List<String> candidateVehicleCodes, boolean recoveryFeasible, String message) {
        this.incidentId = incidentId;
        this.brokenVehicleId = brokenVehicleId;
        this.brokenVehicleCode = brokenVehicleCode;
        this.affectedRouteId = affectedRouteId;
        this.completedStopsCount = completedStopsCount;
        this.affectedOrdersCount = affectedOrdersCount;
        this.affectedOrderIds = affectedOrderIds;
        this.affectedOrderNumbers = affectedOrderNumbers;
        this.candidateVehicleIds = candidateVehicleIds;
        this.candidateVehicleCodes = candidateVehicleCodes;
        this.recoveryFeasible = recoveryFeasible;
        this.message = message;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public UUID getBrokenVehicleId() {
        return brokenVehicleId;
    }

    public void setBrokenVehicleId(UUID brokenVehicleId) {
        this.brokenVehicleId = brokenVehicleId;
    }

    public String getBrokenVehicleCode() {
        return brokenVehicleCode;
    }

    public void setBrokenVehicleCode(String brokenVehicleCode) {
        this.brokenVehicleCode = brokenVehicleCode;
    }

    public UUID getAffectedRouteId() {
        return affectedRouteId;
    }

    public void setAffectedRouteId(UUID affectedRouteId) {
        this.affectedRouteId = affectedRouteId;
    }

    public int getCompletedStopsCount() {
        return completedStopsCount;
    }

    public void setCompletedStopsCount(int completedStopsCount) {
        this.completedStopsCount = completedStopsCount;
    }

    public int getAffectedOrdersCount() {
        return affectedOrdersCount;
    }

    public void setAffectedOrdersCount(int affectedOrdersCount) {
        this.affectedOrdersCount = affectedOrdersCount;
    }

    public List<UUID> getAffectedOrderIds() {
        return affectedOrderIds;
    }

    public void setAffectedOrderIds(List<UUID> affectedOrderIds) {
        this.affectedOrderIds = affectedOrderIds;
    }

    public List<String> getAffectedOrderNumbers() {
        return affectedOrderNumbers;
    }

    public void setAffectedOrderNumbers(List<String> affectedOrderNumbers) {
        this.affectedOrderNumbers = affectedOrderNumbers;
    }

    public List<UUID> getCandidateVehicleIds() {
        return candidateVehicleIds;
    }

    public void setCandidateVehicleIds(List<UUID> candidateVehicleIds) {
        this.candidateVehicleIds = candidateVehicleIds;
    }

    public List<String> getCandidateVehicleCodes() {
        return candidateVehicleCodes;
    }

    public void setCandidateVehicleCodes(List<String> candidateVehicleCodes) {
        this.candidateVehicleCodes = candidateVehicleCodes;
    }

    public boolean isRecoveryFeasible() {
        return recoveryFeasible;
    }

    public void setRecoveryFeasible(boolean recoveryFeasible) {
        this.recoveryFeasible = recoveryFeasible;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
