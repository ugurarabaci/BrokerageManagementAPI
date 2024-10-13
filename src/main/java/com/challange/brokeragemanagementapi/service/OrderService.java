package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.exception.InvalidOrderStatusException;
import com.challange.brokeragemanagementapi.exception.OrderNotFoundException;
import com.challange.brokeragemanagementapi.mapper.OrderConverter;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final CustomerRepository customerRepository;
    private final OrderConverter orderConverter;

    public OrderService(OrderRepository orderRepository, AssetService assetService,
                        CustomerRepository customerRepository, OrderConverter orderConverter) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
        this.customerRepository = customerRepository;
        this.orderConverter = orderConverter;
    }

    public List<OrderDto> listOrders(OrderDto orderDto) {
        LocalDateTime startDateTime = orderDto.getStartDate();
        LocalDateTime endDateTime = orderDto.getEndDate();

        return orderRepository.findOrdersByFilters(
                        orderDto.getCustomerId(),
                        startDateTime,
                        endDateTime,
                        orderDto.getAssetName(),
                        orderDto.getStatus()
                ).stream()
                .map(orderConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Order createOrder(OrderDto orderDto) {

        Customer customer = customerRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Order order = orderConverter.convertToEntity(orderDto, customer);

        assetService.validateAndUpdateAssetForOrder(order);

        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Only PENDING orders can be deleted");
        }

        assetService.revertAssetForDeletedOrder(order);
        orderRepository.delete(order);
    }
}