package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.manager.OrderManager;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.response.CreateOrderResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    //TODO: swagger documentation
    private final OrderManager orderManager;

    public OrderController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#request.customerId)")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse createdOrder = orderManager.createOrder(request);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#customerId)")
    public ResponseEntity<List<OrderDto>> listOrders(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) OrderStatus status) {

        List<OrderDto> orders = orderManager.listOrders(customerId, startDate, endDate, assetName, status);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @securityService.isOwnerOfOrder(#orderId)")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderManager.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}

