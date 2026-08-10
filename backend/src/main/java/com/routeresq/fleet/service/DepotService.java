package com.routeresq.fleet.service;

import com.routeresq.fleet.dto.DepotRequest;
import com.routeresq.fleet.dto.DepotResponse;
import com.routeresq.fleet.mapper.DepotMapper;
import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.shared.dto.PageResponse;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.shared.util.GeometryUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DepotService {

    private final DepotRepository depotRepository;

    public DepotService(DepotRepository depotRepository) {
        this.depotRepository = depotRepository;
    }

    @Transactional
    public DepotResponse createDepot(DepotRequest request) {
        Depot depot = DepotMapper.toEntity(request);
        Depot saved = depotRepository.save(depot);
        return DepotMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DepotResponse getDepot(UUID id) {
        Depot depot = depotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depot", id));
        return DepotMapper.toResponse(depot);
    }

    @Transactional(readOnly = true)
    public PageResponse<DepotResponse> listDepots(Pageable pageable) {
        Page<Depot> page = depotRepository.findAll(pageable);
        return PageResponse.fromPage(page, DepotMapper::toResponse);
    }

    @Transactional
    public DepotResponse updateDepot(UUID id, DepotRequest request) {
        Depot depot = depotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depot", id));

        depot.setName(request.getName());
        depot.setAddressText(request.getAddressText());
        depot.setLocation(GeometryUtils.createPoint(request.getLocation().getLatitude(), request.getLocation().getLongitude()));

        Depot updated = depotRepository.save(depot);
        return DepotMapper.toResponse(updated);
    }

    @Transactional
    public void deleteDepot(UUID id) {
        if (!depotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Depot", id);
        }
        depotRepository.deleteById(id);
    }
}
