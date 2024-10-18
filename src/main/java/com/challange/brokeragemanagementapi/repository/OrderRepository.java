package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findById(Long id);

    List<Order> findByOrderSideAndStatus(OrderSide side, OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId " +
            "AND o.createDate BETWEEN :startDate AND :endDate " +
            "AND (:assetName IS NULL OR o.assetName = :assetName) " +
            "AND (:status IS NULL OR o.status = :status)")
    List<Order> findOrdersByFilters(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("assetName") String assetName,
            @Param("status") OrderStatus status
    );
}

