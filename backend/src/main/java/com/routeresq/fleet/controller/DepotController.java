package com.routeresq.fleet.controller;

import com.routeresq.fleet.dto.DepotRequest;
import com.routeresq.fleet.dto.DepotResponse;
import com.routeresq.fleet.service.DepotService;
import com.routeresq.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/depots")
public class DepotController {

    private final DepotService depotService;

    public DepotController(DepotService depotService) {
        this.depotService = depotService;
    }

    @PostMapping
    public ResponseEntity<DepotResponse> createDepot(@Valid @RequestBody DepotRequest request) {
        DepotResponse response = depotService.createDepot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepotResponse> getDepot(@PathVariable UUID id) {
        DepotResponse response = depotService.getDepot(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<DepotResponse>> listDepots(@PageableDefault(size = 20) Pageable pageable) {
        PageResponse<DepotResponse> response = depotService.listDepots(pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DepotResponse> updateDepot(@PathVariable UUID id, @Valid @RequestBody DepotRequest request) {
        DepotResponse response = depotService.updateDepot(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepot(@PathVariable UUID id) {
        depotService.deleteDepot(id);
        return ResponseEntity.noContent().build();
    }
}
