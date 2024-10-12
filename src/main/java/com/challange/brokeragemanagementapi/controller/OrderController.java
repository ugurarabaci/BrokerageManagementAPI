package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.request.ListOrdersRequest;
import com.challange.brokeragemanagementapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#request.customerId)")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order createdOrder = orderService.createOrder(request);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#request.customerId)")
    public ResponseEntity<List<Order>> listOrders(@Valid @RequestBody ListOrdersRequest request) {
        List<Order> orders = orderService.listOrders(request);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnerOfOrder(#orderId)")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}

