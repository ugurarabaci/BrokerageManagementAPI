package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.*;
import com.challange.brokeragemanagementapi.converter.OrderConverter;
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
        CreateOrderResponse response = new CreateOrderResponse();

        log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}",
                request.getCustomerId(), request.getAssetName(), request.getOrderSide(),
                request.getSize(), request.getPrice());

        try {
            OrderDto orderDto = orderConverter.convertCreateRequestToDTO(request);
            OrderDto createdOrderDTO = orderConverter.convertToDTO(orderService.createOrder(orderDto));

            log.info("Order created successfully. Order ID: {}", createdOrderDTO.getId());

            orderConverter.convertToCreateOrderResponse(createdOrderDTO, response);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());
            response.setMessage("Order created successfully");

        } catch (CustomerNotFoundException | AssetNotFoundException | InsufficientFundsException e) {
            handleKnownExceptions(response, e);
        } catch (Exception e) {
            handleUnknownException(response, e);
        }
        return response;
    }

    public OrderListResponse listOrders(Long customerId, LocalDate startDate, LocalDate endDate, String assetName, OrderStatus status) {
        log.info("Listing orders for customer: {}, from: {}, to: {}, asset: {}, status: {}",
                customerId, startDate, endDate, assetName, status);

        List<OrderDto> orderDtos = orderService.listOrders(orderConverter.convertToDto(customerId, startDate, endDate, assetName, status));

        log.info("Found {} orders matching the criteria", orderDtos.size());

        OrderListResponse orderListResponse = new OrderListResponse();
        orderListResponse.setOrderDtoList(orderDtos);
        orderListResponse.setStatus(ResponseStatusType.SUCCESS.getValue());
        orderListResponse.setMessage("Orders size: " + orderDtos.size());
        return orderListResponse;
    }

    public Response deleteOrder(Long orderId) {
        log.info("Attempting to delete order with ID: {}", orderId);
        Response response = new Response();

        try {
            if (!securityService.isOwnerOfOrder(orderId) && !securityService.isAdmin()) {
                log.warn("User attempted to delete order {} without proper authorization", orderId);
                response.setStatus(ResponseStatusType.FAILURE.getValue());
                response.setMessage("You are not authorized to delete this order");
                return response;
            }

            orderService.deleteOrder(orderId);

            log.info("Order deleted successfully. Order ID: {}", orderId);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());

        } catch (OrderNotFoundException | InvalidOrderStatusException e) {
            handleKnownExceptions(response, e);
        } catch (Exception e) {
            handleUnknownException(response, e);
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
                orderListResponse.setMessage("No matching orders found");
            } else {
                orderListResponse.setOrderDtoList(orderDtoList);
                orderListResponse.setStatus(ResponseStatusType.SUCCESS.getValue());
                log.info("Pending orders matched successfully. Total matched orders: {}", orderDtoList.size());
            }

        } catch (AssetNotFoundException | InsufficientFundsException e) {
            handleKnownExceptions(orderListResponse, e);
        } catch (Exception e) {
            handleUnknownException(orderListResponse, e);
        }

        return orderListResponse;
    }

    private void handleKnownExceptions(Response response, Exception e) {
        log.error(e.getMessage());
        response.setStatus(ResponseStatusType.FAILURE.getValue());
        response.setMessage(e.getMessage());
    }

    private void handleUnknownException(Response response, Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage());
        response.setStatus(ResponseStatusType.FAILURE.getValue());
        response.setMessage("An unexpected error occurred while processing the request");
    }
}
