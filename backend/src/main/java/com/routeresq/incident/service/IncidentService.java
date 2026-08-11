package com.routeresq.incident.service;

import com.routeresq.fleet.model.Vehicle;
import com.routeresq.fleet.repository.VehicleRepository;
import com.routeresq.incident.dto.CreateIncidentRequest;
import com.routeresq.incident.dto.IncidentResponse;
import com.routeresq.incident.model.Incident;
import com.routeresq.incident.model.IncidentStatus;
import com.routeresq.incident.model.IncidentType;
import com.routeresq.incident.repository.IncidentRepository;
import com.routeresq.order.model.Order;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;

    public IncidentService(IncidentRepository incidentRepository,
                           VehicleRepository vehicleRepository,
                           OrderRepository orderRepository) {
        this.incidentRepository = incidentRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));
        }

        Incident incident = Incident.builder()
                .incidentType(request.getIncidentType())
                .status(request.getStatus() != null ? request.getStatus() : IncidentStatus.OPEN)
                .vehicle(vehicle)
                .order(order)
                .description(request.getDescription())
                .occurredAt(Instant.now())
                .build();

        Incident saved = incidentRepository.save(incident);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> listIncidents(IncidentType type, IncidentStatus status) {
        List<Incident> list;
        if (type != null) {
            list = incidentRepository.findByIncidentType(type);
        } else if (status != null) {
            list = incidentRepository.findByStatus(status);
        } else {
            list = incidentRepository.findAll();
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return mapToResponse(incident);
    }

    private IncidentResponse mapToResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentType(),
                incident.getStatus(),
                incident.getVehicle() != null ? incident.getVehicle().getId() : null,
                incident.getVehicle() != null ? incident.getVehicle().getVehicleCode() : null,
                incident.getOrder() != null ? incident.getOrder().getId() : null,
                incident.getOrder() != null ? incident.getOrder().getOrderNumber() : null,
                incident.getDescription(),
                incident.getOccurredAt(),
                incident.getCreatedAt()
        );
    }
}
