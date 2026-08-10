package com.routeresq.order.service;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.order.dto.CreateOrderRequest;
import com.routeresq.order.dto.OrderResponse;
import com.routeresq.order.dto.UpdateOrderRequest;
import com.routeresq.order.mapper.OrderMapper;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.shared.dto.PageResponse;
import com.routeresq.shared.exception.InvalidStatusTransitionException;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DepotRepository depotRepository;

    public OrderService(OrderRepository orderRepository, DepotRepository depotRepository) {
        this.orderRepository = orderRepository;
        this.depotRepository = depotRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot", request.getDepotId()));

        Order order = OrderMapper.toEntity(request, depot);
        order.validateTimeWindow();

        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(OrderStatus status, UUID depotId, Pageable pageable) {
        Page<Order> page;
        if (status != null && depotId != null) {
            page = orderRepository.findByStatusAndDepotId(status, depotId, pageable);
        } else if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else if (depotId != null) {
            page = orderRepository.findByDepotId(depotId, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return PageResponse.fromPage(page, OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getNearbyOrders(double latitude, double longitude, double radiusMeters) {
        Point point = GeometryUtils.createPoint(latitude, longitude);
        List<Order> nearby = orderRepository.findOrdersWithinRadius(point, radiusMeters);
        return nearby.stream().map(OrderMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (request.getStatus() != null && request.getStatus() != order.getStatus()) {
            validateStatusTransition(order.getStatus(), request.getStatus());
            order.setStatus(request.getStatus());
        }

        if (request.getDepotId() != null) {
            Depot depot = depotRepository.findById(request.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot", request.getDepotId()));
            order.setDepot(depot);
        }

        if (request.getOrderNumber() != null) order.setOrderNumber(request.getOrderNumber());
        if (request.getCustomerName() != null) order.setCustomerName(request.getCustomerName());
        if (request.getAddressText() != null) order.setAddressText(request.getAddressText());
        if (request.getWeightKg() != null) order.setWeightKg(request.getWeightKg());
        if (request.getVolumeM3() != null) order.setVolumeM3(request.getVolumeM3());
        if (request.getWindowStartMinutes() != null) order.setWindowStartMinutes(request.getWindowStartMinutes());
        if (request.getWindowEndMinutes() != null) order.setWindowEndMinutes(request.getWindowEndMinutes());
        if (request.getServiceDurationMinutes() != null) order.setServiceDurationMinutes(request.getServiceDurationMinutes());
        if (request.getPriority() != null) order.setPriority(request.getPriority());

        if (request.getLocation() != null) {
            order.setLocation(GeometryUtils.createPoint(
                    request.getLocation().getLatitude(),
                    request.getLocation().getLongitude()
            ));
        }

        order.validateTimeWindow();

        Order updated = orderRepository.save(order);
        return OrderMapper.toResponse(updated);
    }

    @Transactional
    public void deleteOrder(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", id);
        }
        orderRepository.deleteById(id);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case UNASSIGNED -> (targetStatus == OrderStatus.ASSIGNED || targetStatus == OrderStatus.CANCELLED);
            case ASSIGNED -> (targetStatus == OrderStatus.IN_TRANSIT || targetStatus == OrderStatus.UNASSIGNED || targetStatus == OrderStatus.CANCELLED);
            case IN_TRANSIT -> (targetStatus == OrderStatus.DELIVERED || targetStatus == OrderStatus.FAILED);
            case FAILED -> (targetStatus == OrderStatus.UNASSIGNED || targetStatus == OrderStatus.CANCELLED);
            case DELIVERED, CANCELLED -> false; // Terminal states
        };

        if (!valid) {
            throw new InvalidStatusTransitionException("Order", currentStatus, targetStatus);
        }
    }
}
