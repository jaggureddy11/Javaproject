package com.routeresq.order.mapper;

import com.routeresq.fleet.model.Depot;
import com.routeresq.order.dto.CreateOrderRequest;
import com.routeresq.order.dto.OrderResponse;
import com.routeresq.order.model.Order;
import com.routeresq.shared.dto.LocationDto;
import com.routeresq.shared.util.GeometryUtils;

public class OrderMapper {

    public static Order toEntity(CreateOrderRequest request, Depot depot) {
        return Order.builder()
                .orderNumber(request.getOrderNumber())
                .depot(depot)
                .customerName(request.getCustomerName())
                .location(GeometryUtils.createPoint(request.getLocation().getLatitude(), request.getLocation().getLongitude()))
                .addressText(request.getAddressText())
                .weightKg(request.getWeightKg())
                .volumeM3(request.getVolumeM3())
                .windowStartMinutes(request.getWindowStartMinutes())
                .windowEndMinutes(request.getWindowEndMinutes())
                .serviceDurationMinutes(request.getServiceDurationMinutes())
                .priority(request.getPriority())
                .status(request.getStatus())
                .build();
    }

    public static OrderResponse toResponse(Order order) {
        LocationDto location = new LocationDto(
                GeometryUtils.getLatitude(order.getLocation()),
                GeometryUtils.getLongitude(order.getLocation())
        );

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getDepot() != null ? order.getDepot().getId() : null,
                order.getDepot() != null ? order.getDepot().getName() : null,
                order.getCustomerName(),
                location,
                order.getAddressText(),
                order.getWeightKg(),
                order.getVolumeM3(),
                order.getWindowStartMinutes(),
                order.getWindowEndMinutes(),
                order.getServiceDurationMinutes(),
                order.getPriority(),
                order.getStatus(),
                order.getVersion(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
