package com.routeresq.order.repository;

import com.routeresq.order.model.Order;
import com.routeresq.order.model.OrderStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByDepotId(UUID depotId);

    Page<Order> findByDepotId(UUID depotId, Pageable pageable);

    List<Order> findByDepotIdAndStatus(UUID depotId, OrderStatus status);

    Page<Order> findByStatusAndDepotId(OrderStatus status, UUID depotId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE ST_DWithin(o.location, :point, :radiusMeters) = true")
    List<Order> findOrdersWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);

    @Query("SELECT o FROM Order o WHERE o.depot.id = :depotId AND o.status IN :statuses")
    List<Order> findByDepotIdAndStatusIn(@Param("depotId") UUID depotId, @Param("statuses") List<OrderStatus> statuses);

    @Query(value = "SELECT o.id, ST_DistanceSphere(o.location, :point) AS distance_meters FROM orders o WHERE o.id = :orderId", nativeQuery = true)
    Double calculateDistanceToOrder(@Param("orderId") UUID orderId, @Param("point") Point point);
}
