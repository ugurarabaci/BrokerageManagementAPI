package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.manager.OrderManager;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.enumtype.ResponseStatusType;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.response.CreateOrderResponse;
import com.challange.brokeragemanagementapi.model.response.OrderListResponse;
import com.challange.brokeragemanagementapi.model.response.Response;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "For managing orders")
public class OrderController {
    private final OrderManager orderManager;

    public OrderController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#request.customerId)")
    @Operation(summary = "Create Order", description = "Create a new order for a customer")
    @ApiResponse(responseCode = "200", description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = CreateOrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse createdOrder = orderManager.createOrder(request);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#customerId)")
    @Operation(summary = "List orders", description = "Lists orders for a given customer and date range")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(schema = @Schema(implementation = OrderDto.class)))
    public ResponseEntity<OrderListResponse> listOrders(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) OrderStatus status) {

        OrderListResponse orderListResponse = orderManager.listOrders(customerId, startDate, endDate, assetName, status);
        return ResponseEntity.ok(orderListResponse);
    }

    @PostMapping("/match")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "matchPendingOrders", fallbackMethod = "rateLimiterFallback")
    @ApiResponse(responseCode = "200", description = "Orders matched successfully",
            content = @Content(schema = @Schema(implementation = OrderListResponse.class)))
    @Operation(summary = "Match pending orders", description = "Match pending buy and sell orders(Rate limited 5 request in 1 min)")
    public ResponseEntity<OrderListResponse> matchPendingOrders() {
        OrderListResponse matchedOrders = orderManager.matchPendingOrders();
        return ResponseEntity.ok(matchedOrders);
    }


    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#orderId)")
    @ApiResponse(responseCode = "200", description = "Order deleted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid order ID or order cannot be deleted")
    public ResponseEntity<Response> deleteOrder(@PathVariable Long orderId) {
        Response response = orderManager.deleteOrder(orderId);
        if (Objects.equals(response.getStatus(), ResponseStatusType.SUCCESS.getValue())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    public ResponseEntity<String> rateLimiterFallback(Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests - please try again later.");
    }
}

