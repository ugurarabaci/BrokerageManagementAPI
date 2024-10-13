package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.exception.InvalidOrderStatusException;
import com.challange.brokeragemanagementapi.exception.OrderNotFoundException;
import com.challange.brokeragemanagementapi.mapper.AssetConverter;
import com.challange.brokeragemanagementapi.mapper.OrderConverter;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AssetService assetService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderConverter orderConverter;
    @Mock
    private AssetConverter assetConverter;

    @InjectMocks
    private OrderService orderService;

    @Test
    void should_list_order() {
        OrderDto filterDto = new OrderDto();
        filterDto.setCustomerId(1L);
        filterDto.setStartDate(LocalDateTime.now().minusDays(1));
        filterDto.setEndDate(LocalDateTime.now());
        filterDto.setAssetName("AAPL");
        filterDto.setStatus(OrderStatus.PENDING);

        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findOrdersByFilters(
                eq(filterDto.getCustomerId()),
                eq(filterDto.getStartDate()),
                eq(filterDto.getEndDate()),
                eq(filterDto.getAssetName()),
                eq(filterDto.getStatus())
        )).thenReturn(orders);

        OrderDto orderDto1 = new OrderDto();
        OrderDto orderDto2 = new OrderDto();
        when(orderConverter.convertToDTO(order1)).thenReturn(orderDto1);
        when(orderConverter.convertToDTO(order2)).thenReturn(orderDto2);

        List<OrderDto> result = orderService.listOrders(filterDto);

        assertEquals(2, result.size());
        verify(orderRepository).findOrdersByFilters(
                eq(filterDto.getCustomerId()),
                eq(filterDto.getStartDate()),
                eq(filterDto.getEndDate()),
                eq(filterDto.getAssetName()),
                eq(filterDto.getStatus())
        );
        verify(orderConverter, times(2)).convertToDTO(any(Order.class));
    }

    @Test
    void should_create_order() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId(1L);

        Customer customer = new Customer();
        Order order = new Order();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderConverter.convertToEntity(orderDto, customer)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(orderDto);

        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(orderConverter).convertToEntity(orderDto, customer);
        verify(assetService).validateAndUpdateAssetForOrder(order);
        verify(orderRepository).save(order);
    }

    @Test
    void should_throw_customer_not_found_exception() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> orderService.createOrder(orderDto));
        verify(customerRepository).findById(1L);
        verifyNoInteractions(orderConverter, assetService, orderRepository);
    }

    @Test
    void should_delete_pending_order() {
        Long orderId = 1L;
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrder(orderId);

        verify(orderRepository).findById(orderId);
        verify(assetService).revertAssetForDeletedOrder(order);
        verify(orderRepository).delete(order);
    }

    @Test
    void should_throw_order_not_found_exception() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(orderId));
        verify(orderRepository).findById(orderId);
        verifyNoInteractions(assetService);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void should_throw_invalid_order_status_exception() {
        Long orderId = 1L;
        Order order = new Order();
        order.setStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.deleteOrder(orderId));
        verify(orderRepository).findById(orderId);
        verifyNoInteractions(assetService);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void should_return_filtered_orders() {
        // Arrange
        OrderDto filterDto = new OrderDto();
        filterDto.setCustomerId(1L);
        filterDto.setStartDate(LocalDateTime.of(2023, 1, 1, 0, 0));
        filterDto.setEndDate(LocalDateTime.of(2023, 12, 31, 23, 59));
        filterDto.setAssetName("BTC");
        filterDto.setStatus(OrderStatus.PENDING);

        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);
        List<Order> orders = Arrays.asList(order1, order2);

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(1L);
        OrderDto orderDto2 = new OrderDto();
        orderDto2.setId(2L);

        when(orderRepository.findOrdersByFilters(
                eq(filterDto.getCustomerId()),
                eq(filterDto.getStartDate()),
                eq(filterDto.getEndDate()),
                eq(filterDto.getAssetName()),
                eq(filterDto.getStatus())
        )).thenReturn(orders);

        when(orderConverter.convertToDTO(order1)).thenReturn(orderDto1);
        when(orderConverter.convertToDTO(order2)).thenReturn(orderDto2);

        // Act
        List<OrderDto> result = orderService.listOrders(filterDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(orderRepository).findOrdersByFilters(
                eq(filterDto.getCustomerId()),
                eq(filterDto.getStartDate()),
                eq(filterDto.getEndDate()),
                eq(filterDto.getAssetName()),
                eq(filterDto.getStatus())
        );
        verify(orderConverter, times(2)).convertToDTO(any(Order.class));
    }

}

