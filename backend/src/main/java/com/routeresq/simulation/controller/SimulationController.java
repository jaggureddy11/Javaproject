package com.routeresq.simulation.controller;

import com.routeresq.simulation.dto.CreateSimulationRequest;
import com.routeresq.simulation.dto.SimulationSessionResponse;
import com.routeresq.simulation.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<SimulationSessionResponse> createSession(@Valid @RequestBody CreateSimulationRequest request) {
        SimulationSessionResponse response = simulationService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SimulationSessionResponse> startSession(@PathVariable UUID id) {
        SimulationSessionResponse response = simulationService.startSession(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<SimulationSessionResponse> pauseSession(@PathVariable UUID id) {
        SimulationSessionResponse response = simulationService.pauseSession(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<SimulationSessionResponse> resumeSession(@PathVariable UUID id) {
        SimulationSessionResponse response = simulationService.resumeSession(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<SimulationSessionResponse> stopSession(@PathVariable UUID id) {
        SimulationSessionResponse response = simulationService.stopSession(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulationSessionResponse> getSession(@PathVariable UUID id) {
        SimulationSessionResponse response = simulationService.getSessionState(id);
        return ResponseEntity.ok(response);
    }
}
