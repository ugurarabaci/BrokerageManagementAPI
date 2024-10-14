package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.*;
import com.challange.brokeragemanagementapi.mapper.OrderConverter;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.enumtype.ResponseStatusType;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.response.CreateOrderResponse;
import com.challange.brokeragemanagementapi.model.response.OrderListResponse;
import com.challange.brokeragemanagementapi.model.response.Response;
import com.challange.brokeragemanagementapi.security.SecurityService;
import com.challange.brokeragemanagementapi.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        try {
            log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}",
                    request.getCustomerId(), request.getAssetName(), request.getOrderSide(),
                    request.getSize(), request.getPrice());

            OrderDto orderDto = orderConverter.convertCreateRequestToDTO(request);
            OrderDto createdOrderDTO = orderConverter.convertToDTO(orderService.createOrder(orderDto));

            log.info("Order created successfully. Order ID: {}", createdOrderDTO.getId());

            CreateOrderResponse response = orderConverter.convertToCreateOrderResponse(createdOrderDTO);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());

            return response;

        } catch (CustomerNotFoundException e) {
            log.error("Customer not found while creating order: {}", e.getMessage());
            CreateOrderResponse response = new CreateOrderResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("Customer not found: " + e.getMessage());
            return response;
        } catch (AssetNotFoundException e) {
            log.error("Asset not found while creating order: {}", e.getMessage());
            CreateOrderResponse response = new CreateOrderResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("Asset not found: " + e.getMessage());
            return response;
        } catch (InsufficientFundsException e) {
            log.error("Insufficient funds while creating order: {}", e.getMessage());
            CreateOrderResponse response = new CreateOrderResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("Insufficient funds: " + e.getMessage());
            return response;
        } catch (Exception e) {
            log.error("Unexpected error while creating order: {}", e.getMessage());
            CreateOrderResponse response = new CreateOrderResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("An unexpected error occurred while creating order");
            return response;
        }
    }

    public OrderListResponse listOrders(Long customerId, LocalDate startDate, LocalDate endDate, String assetName, OrderStatus status) {
        log.info("Listing orders for customer: {}, from: {}, to: {}, asset: {}, status: {}",
                customerId, startDate, endDate, assetName, status);

        List<OrderDto> orderDtos = orderService.listOrders(orderConverter.convertToDto(customerId, startDate, endDate, assetName, status));

        log.info("Found {} orders matching the criteria", orderDtos.size());

        OrderListResponse orderListResponse = new OrderListResponse();
        orderListResponse.setOrderDtoList(orderDtos);
        orderListResponse.setStatus(ResponseStatusType.SUCCESS.getValue());
        return orderListResponse;
    }

    public Response deleteOrder(Long orderId) {
        log.info("Attempting to delete order with ID: {}", orderId);
        Response response = new Response();

        try {
            if (!securityService.isOwnerOfOrder(orderId) && !securityService.isAdmin()) {
                log.warn("User attempted to delete order {} without proper authorization", orderId);
                response.setStatus(ResponseStatusType.FAILURE.getValue());
                response.setErrorMessage("You are not authorized to delete this order");
                return response;
            }

            orderService.deleteOrder(orderId);

            log.info("Order deleted successfully. Order ID: {}", orderId);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());
        } catch (OrderNotFoundException e) {
            log.error("Order not found for deletion. Order ID: {}", orderId);
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage(e.getMessage());
        } catch (InvalidOrderStatusException e) {
            log.error("Only PENDING orders can be deleted. Order ID: {}", orderId);
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete order with ID: {}. Error: {}", orderId, e.getMessage());
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("An unexpected error occurred while deleting the order");
        }

        return response;
    }

    public OrderListResponse matchPendingOrders() {
        log.info("Starting to match pending orders");
        OrderListResponse orderListResponse = new OrderListResponse();

        try {
            List<OrderDto> orderDtoList = orderService.matchPendingOrders();

            if (orderDtoList.isEmpty()) {
                log.info("No orders were matched");
                orderListResponse.setStatus(ResponseStatusType.NO_MATCH.getValue());
                orderListResponse.setErrorMessage("No matching orders found");
            } else {
                orderListResponse.setOrderDtoList(orderDtoList);
                orderListResponse.setStatus(ResponseStatusType.SUCCESS.getValue());
                log.info("Pending orders matched successfully. Total matched orders: {}", orderDtoList.size());
            }
        } catch (AssetNotFoundException e) {
            log.error("Asset not found during order matching: {}", e.getMessage());
            orderListResponse.setStatus(ResponseStatusType.FAILURE.getValue());
            orderListResponse.setErrorMessage("Asset not found: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            log.error("Insufficient funds during order matching: {}", e.getMessage());
            orderListResponse.setStatus(ResponseStatusType.FAILURE.getValue());
            orderListResponse.setErrorMessage("Insufficient funds: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during order matching: {}", e.getMessage(), e);
            orderListResponse.setStatus(ResponseStatusType.FAILURE.getValue());
            orderListResponse.setErrorMessage("An unexpected error occurred: " + e.getMessage());
        }
        return orderListResponse;
    }
}