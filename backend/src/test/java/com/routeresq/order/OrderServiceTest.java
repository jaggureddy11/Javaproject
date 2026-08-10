package com.routeresq.order;

import com.routeresq.fleet.model.Depot;
import com.routeresq.fleet.repository.DepotRepository;
import com.routeresq.order.dto.CreateOrderRequest;
import com.routeresq.order.dto.OrderResponse;
import com.routeresq.order.dto.UpdateOrderRequest;
import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import com.routeresq.order.repository.OrderRepository;
import com.routeresq.order.service.OrderService;
import com.routeresq.shared.dto.LocationDto;
import com.routeresq.shared.exception.InvalidStatusTransitionException;
import com.routeresq.shared.exception.ResourceNotFoundException;
import com.routeresq.shared.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private DepotRepository depotRepository;
    private OrderService orderService;
    private Depot testDepot;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        depotRepository = mock(DepotRepository.class);
        orderService = new OrderService(orderRepository, depotRepository);

        testDepot = Depot.builder()
                .name("Central Depot")
                .location(GeometryUtils.createPoint(41.8781, -87.6298))
                .addressText("100 S Wacker Dr")
                .build();
        testDepot.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Create Order Success")
    void testCreateOrderSuccess() {
        CreateOrderRequest request = new CreateOrderRequest(
                "ORD-999",
                testDepot.getId(),
                "Acme Inc",
                new LocationDto(41.8850, -87.6300),
                "123 Lake St",
                new BigDecimal("15.50"),
                new BigDecimal("0.20"),
                540,
                660,
                10,
                1,
                OrderStatus.UNASSIGNED
        );

        when(depotRepository.findById(testDepot.getId())).thenReturn(Optional.of(testDepot));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order arg = invocation.getArgument(0);
            arg.setId(UUID.randomUUID());
            return arg;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-999");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.UNASSIGNED);
        assertThat(response.getLocation().getLatitude()).isEqualTo(41.8850);
    }

    @Test
    @DisplayName("Create Order Rejects Invalid Time Window")
    void testCreateOrderInvalidTimeWindow() {
        CreateOrderRequest request = new CreateOrderRequest(
                "ORD-BAD",
                testDepot.getId(),
                "Acme Inc",
                new LocationDto(41.8850, -87.6300),
                "123 Lake St",
                new BigDecimal("15.50"),
                new BigDecimal("0.20"),
                700, // windowStart > windowEnd
                600,
                10,
                1,
                OrderStatus.UNASSIGNED
        );

        when(depotRepository.findById(testDepot.getId())).thenReturn(Optional.of(testDepot));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be strictly less than window end minutes");
    }

    @Test
    @DisplayName("Update Order Status Transition Validation")
    void testInvalidStatusTransition() {
        Order existingOrder = Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-100")
                .customerName("Customer A")
                .location(GeometryUtils.createPoint(41.8850, -87.6300))
                .addressText("123 Address")
                .weightKg(new BigDecimal("10.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(660)
                .status(OrderStatus.DELIVERED) // Terminal state
                .build();
        existingOrder.setId(UUID.randomUUID());

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setStatus(OrderStatus.UNASSIGNED); // Illegal transition DELIVERED -> UNASSIGNED

        assertThatThrownBy(() -> orderService.updateOrder(existingOrder.getId(), updateRequest))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot transition Order status from 'DELIVERED' to 'UNASSIGNED'");
    }

    @Test
    @DisplayName("Update Order Valid Status Transition UNASSIGNED -> ASSIGNED")
    void testValidStatusTransition() {
        Order existingOrder = Order.builder()
                .depot(testDepot)
                .orderNumber("ORD-101")
                .customerName("Customer B")
                .location(GeometryUtils.createPoint(41.8850, -87.6300))
                .addressText("123 Address")
                .weightKg(new BigDecimal("10.00"))
                .windowStartMinutes(540)
                .windowEndMinutes(660)
                .status(OrderStatus.UNASSIGNED)
                .build();
        existingOrder.setId(UUID.randomUUID());

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setStatus(OrderStatus.ASSIGNED);

        OrderResponse response = orderService.updateOrder(existingOrder.getId(), updateRequest);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }
}
