package com.challange.brokeragemanagementapi.mapper;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.response.CreateOrderResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class OrderConverter {

    public OrderDto convertToDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        dto.setAssetName(order.getAssetName());
        dto.setOrderSide(order.getOrderSide());
        dto.setSize(order.getSize());
        dto.setPrice(order.getPrice());
        dto.setStatus(order.getStatus());
        dto.setCreateDate(order.getCreateDate());

        return dto;
    }

    public Order convertToEntity(OrderDto dto, Customer customer) {
        if (dto == null) {
            return null;
        }

        Order order = new Order();
        order.setId(dto.getId());
        order.setAssetName(dto.getAssetName());
        order.setOrderSide(dto.getOrderSide());
        order.setSize(dto.getSize());
        order.setPrice(dto.getPrice());
        order.setStatus(dto.getStatus());
        order.setCreateDate(dto.getCreateDate());

        // Customer nesnesini ayrıca set etmek gerekecek

        return order;
    }

    public OrderDto convertCreateRequestToDTO(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }

        OrderDto dto = new OrderDto();
        dto.setCustomerId(request.getCustomerId());
        dto.setAssetName(request.getAssetName());
        dto.setOrderSide(request.getOrderSide());
        dto.setSize(request.getSize());
        dto.setPrice(request.getPrice());
        dto.setStatus(OrderStatus.PENDING);  // Varsayılan durum
        dto.setCreateDate(LocalDateTime.now());

        return dto;
    }

    public OrderDto convertToDto(Long customerId, LocalDate startDate, LocalDate endDate, String assetName, OrderStatus status) {

        OrderDto dto = new OrderDto();
        dto.setCustomerId(customerId);
        dto.setAssetName(assetName);
        dto.setStatus(OrderStatus.PENDING);  // Varsayılan durum
        dto.setCreateDate(LocalDateTime.now());
        dto.setStartDate(startDate.atStartOfDay());
        dto.setEndDate(endDate.atStartOfDay());

        return dto;
    }

    public CreateOrderResponse convertToCreateOrderResponse(OrderDto dto) {
        if (dto == null) {
            return null;
        }

        CreateOrderResponse response = new CreateOrderResponse();
        response.setId(dto.getId());
        response.setCustomerId(dto.getCustomerId());
        response.setAssetName(dto.getAssetName());
        response.setOrderSide(dto.getOrderSide());
        response.setSize(dto.getSize());
        response.setPrice(dto.getPrice());
        response.setOrderStatus(dto.getStatus());
        response.setCreateDate(dto.getCreateDate());

        return response;
    }

}
