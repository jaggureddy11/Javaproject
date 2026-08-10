package com.routeresq.fleet.service;

import com.routeresq.fleet.dto.DriverRequest;
import com.routeresq.fleet.dto.DriverResponse;
import com.routeresq.fleet.mapper.DriverMapper;
import com.routeresq.fleet.model.Driver;
import com.routeresq.fleet.model.DriverStatus;
import com.routeresq.fleet.repository.DriverRepository;
import com.routeresq.shared.dto.PageResponse;
import com.routeresq.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        Driver driver = DriverMapper.toEntity(request);
        driver.validateShift();

        Driver saved = driverRepository.save(driver);
        return DriverMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
        return DriverMapper.toResponse(driver);
    }

    @Transactional(readOnly = true)
    public PageResponse<DriverResponse> listDrivers(DriverStatus status, Pageable pageable) {
        Page<Driver> page;
        if (status != null) {
            page = driverRepository.findByStatus(status, pageable);
        } else {
            page = driverRepository.findAll(pageable);
        }
        return PageResponse.fromPage(page, DriverMapper::toResponse);
    }

    @Transactional
    public DriverResponse updateDriver(UUID id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));

        driver.setName(request.getName());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setPhone(request.getPhone());
        driver.setStatus(request.getStatus());
        driver.setShiftStartMinutes(request.getShiftStartMinutes());
        driver.setShiftEndMinutes(request.getShiftEndMinutes());

        driver.validateShift();

        Driver updated = driverRepository.save(driver);
        return DriverMapper.toResponse(updated);
    }

    @Transactional
    public void deleteDriver(UUID id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver", id);
        }
        driverRepository.deleteById(id);
    }
}
