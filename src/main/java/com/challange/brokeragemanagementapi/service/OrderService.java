package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.model.request.CreateOrderRequest;
import com.challange.brokeragemanagementapi.model.request.ListOrdersRequest;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, AssetService assetService,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
        this.customerRepository = customerRepository;
    }

    public List<Order> listOrders(ListOrdersRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        return orderRepository.findOrdersByFilters(
                request.getCustomerId(),
                startDateTime,
                endDateTime,
                request.getAssetName(),
                request.getStatus()
        );
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomer(customerRepository.findById(request.getCustomerId()).get());
        order.setAssetName(request.getAssetName());
        order.setOrderSide(request.getOrderSide());
        order.setSize(request.getSize());
        order.setPrice(request.getPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());

        assetService.validateAndUpdateAssetForOrder(order);

        return orderRepository.save(order);
    }
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be deleted");
        }

        assetService.revertAssetForDeletedOrder(order);
        orderRepository.delete(order);
    }
}