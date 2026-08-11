package com.routeresq.incident.controller;

import com.routeresq.incident.dto.CreateIncidentRequest;
import com.routeresq.incident.dto.ImpactAnalysisResult;
import com.routeresq.incident.dto.IncidentResponse;
import com.routeresq.incident.dto.RecoveryPlanResponse;
import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;
import com.routeresq.incident.service.IncidentRecoveryService;
import com.routeresq.incident.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentRecoveryService incidentRecoveryService;

    public IncidentController(IncidentService incidentService, IncidentRecoveryService incidentRecoveryService) {
        this.incidentService = incidentService;
        this.incidentRecoveryService = incidentRecoveryService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> listIncidents(
            @RequestParam(required = false) IncidentType type,
            @RequestParam(required = false) IncidentStatus status) {
        List<IncidentResponse> list = incidentService.listIncidents(type, status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable UUID id) {
        IncidentResponse response = incidentService.getIncident(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ImpactAnalysisResult> analyzeIncident(@PathVariable UUID id) {
        ImpactAnalysisResult result = incidentRecoveryService.analyzeIncident(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/recover")
    public ResponseEntity<RecoveryPlanResponse> recoverIncident(@PathVariable UUID id) {
        RecoveryPlanResponse response = incidentRecoveryService.recoverIncident(id);
        return ResponseEntity.ok(response);
    }
}
