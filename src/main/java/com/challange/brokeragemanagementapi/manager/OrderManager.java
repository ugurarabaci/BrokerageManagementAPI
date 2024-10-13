package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.mapper.OrderConverter;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.response.CreateOrderResponse;
import com.challange.brokeragemanagementapi.security.SecurityService;
import com.challange.brokeragemanagementapi.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class OrderManager {

    private final OrderService orderService;
    private final OrderConverter orderConverter;

    private final SecurityService securityService;

    public OrderManager(OrderService orderService, OrderConverter orderConverter, SecurityService securityService) {
        this.orderService = orderService;
        this.orderConverter = orderConverter;
        this.securityService = securityService;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}",
                request.getCustomerId(), request.getAssetName(), request.getOrderSide(),
                request.getSize(), request.getPrice());

        OrderDto orderDto = orderConverter.convertCreateRequestToDTO(request);

        OrderDto createdOrderDTO = orderConverter.convertToDTO(orderService.createOrder(orderDto));

        log.info("Order created successfully. Order ID: {}", createdOrderDTO.getId());

        return orderConverter.convertToCreateOrderResponse(createdOrderDTO);
    }

    public List<OrderDto> listOrders(Long customerId, LocalDate startDate, LocalDate endDate, String assetName, OrderStatus status) {
        log.info("Listing orders for customer: {}, from: {}, to: {}, asset: {}, status: {}",
                customerId, startDate, endDate, assetName, status);

        List<OrderDto> orderDtos = orderService.listOrders(orderConverter.convertToDto(customerId, startDate, endDate, assetName, status));

        log.info("Found {} orders matching the criteria", orderDtos.size());

        return orderDtos;
    }

    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);
        if (!securityService.isOwnerOfOrder(orderId)) {
            log.warn("User attempted to delete order {} without ownership", orderId);
            try {
                throw new AccessDeniedException("You are not the owner of this order");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        orderService.deleteOrder(orderId);

        log.info("Order deleted successfully. Order ID: {}", orderId);
    }
}