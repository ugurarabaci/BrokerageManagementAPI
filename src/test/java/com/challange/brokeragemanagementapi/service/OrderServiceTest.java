package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.*;
import com.challange.brokeragemanagementapi.converter.AssetConverter;
import com.challange.brokeragemanagementapi.converter.OrderConverter;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.repository.AssetRepository;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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

    @Mock
    private AssetRepository assetRepository;

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

    @Test
    void should_match_pending_orders() {
        // Arrange
        Customer buyer = new Customer();
        buyer.setId(1L);
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = new Order();
        buyOrder.setId(1L);
        buyOrder.setCustomer(buyer);
        buyOrder.setAssetName("BTC");
        buyOrder.setOrderSide(OrderSide.BUY);
        buyOrder.setSize(new BigDecimal("1.0"));
        buyOrder.setPrice(new BigDecimal("50000"));
        buyOrder.setStatus(OrderStatus.PENDING);

        Order sellOrder = new Order();
        sellOrder.setId(2L);
        sellOrder.setCustomer(seller);
        sellOrder.setAssetName("BTC");
        sellOrder.setOrderSide(OrderSide.SELL);
        sellOrder.setSize(new BigDecimal("1.0"));
        sellOrder.setPrice(new BigDecimal("49000"));
        sellOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByOrderSideAndStatus(OrderSide.BUY, OrderStatus.PENDING))
                .thenReturn(Arrays.asList(buyOrder));
        when(orderRepository.findByOrderSideAndStatus(OrderSide.SELL, OrderStatus.PENDING))
                .thenReturn(Arrays.asList(sellOrder));

        Asset buyerBTC = new Asset();
        buyerBTC.setCustomer(buyer);
        buyerBTC.setAssetName("BTC");
        buyerBTC.setSize(BigDecimal.ZERO);
        buyerBTC.setUsableSize(BigDecimal.ZERO);

        Asset sellerBTC = new Asset();
        sellerBTC.setCustomer(seller);
        sellerBTC.setAssetName("BTC");
        sellerBTC.setSize(new BigDecimal("1.0"));
        sellerBTC.setUsableSize(new BigDecimal("1.0"));

        Asset buyerTRY = new Asset();
        buyerTRY.setCustomer(buyer);
        buyerTRY.setAssetName("TRY");
        buyerTRY.setSize(new BigDecimal("100000"));
        buyerTRY.setUsableSize(new BigDecimal("100000"));

        Asset sellerTRY = new Asset();
        sellerTRY.setCustomer(seller);
        sellerTRY.setAssetName("TRY");
        sellerTRY.setSize(new BigDecimal("10000"));
        sellerTRY.setUsableSize(new BigDecimal("10000"));

        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "BTC")).thenReturn(Optional.of(buyerBTC));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "BTC")).thenReturn(Optional.of(sellerBTC));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "TRY")).thenReturn(Optional.of(buyerTRY));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "TRY")).thenReturn(Optional.of(sellerTRY));

        OrderDto buyOrderDto = new OrderDto();
        OrderDto sellOrderDto = new OrderDto();
        when(orderConverter.convertToDTO(buyOrder)).thenReturn(buyOrderDto);
        when(orderConverter.convertToDTO(sellOrder)).thenReturn(sellOrderDto);

        // Act
        List<OrderDto> result = orderService.matchPendingOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(assetRepository, times(4)).save(any(Asset.class));
        assertEquals(OrderStatus.MATCHED, buyOrder.getStatus());
        assertEquals(OrderStatus.MATCHED, sellOrder.getStatus());
    }

    @Test
    void should_match_pending_orders_no_match() {
        // Arrange
        when(orderRepository.findByOrderSideAndStatus(any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<OrderDto> result = orderService.matchPendingOrders();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void should_match_pending_orders_partial_match() {
        // Arrange
        Customer buyer = new Customer();
        buyer.setId(1L);
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = new Order();
        buyOrder.setId(1L);
        buyOrder.setCustomer(buyer);
        buyOrder.setAssetName("BTC");
        buyOrder.setOrderSide(OrderSide.BUY);
        buyOrder.setSize(new BigDecimal("2.0"));
        buyOrder.setPrice(new BigDecimal("50000"));
        buyOrder.setStatus(OrderStatus.PENDING);

        Order sellOrder = new Order();
        sellOrder.setId(2L);
        sellOrder.setCustomer(seller);
        sellOrder.setAssetName("BTC");
        sellOrder.setOrderSide(OrderSide.SELL);
        sellOrder.setSize(new BigDecimal("1.0"));
        sellOrder.setPrice(new BigDecimal("49000"));
        sellOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByOrderSideAndStatus(OrderSide.BUY, OrderStatus.PENDING))
                .thenReturn(Arrays.asList(buyOrder));
        when(orderRepository.findByOrderSideAndStatus(OrderSide.SELL, OrderStatus.PENDING))
                .thenReturn(Arrays.asList(sellOrder));

        Asset buyerBTC = new Asset();
        buyerBTC.setCustomer(buyer);
        buyerBTC.setAssetName("BTC");
        buyerBTC.setSize(BigDecimal.ZERO);
        buyerBTC.setUsableSize(BigDecimal.ZERO);

        Asset sellerBTC = new Asset();
        sellerBTC.setCustomer(seller);
        sellerBTC.setAssetName("BTC");
        sellerBTC.setSize(new BigDecimal("1.0"));
        sellerBTC.setUsableSize(new BigDecimal("1.0"));

        Asset buyerTRY = new Asset();
        buyerTRY.setCustomer(buyer);
        buyerTRY.setAssetName("TRY");
        buyerTRY.setSize(new BigDecimal("100000"));
        buyerTRY.setUsableSize(new BigDecimal("100000"));

        Asset sellerTRY = new Asset();
        sellerTRY.setCustomer(seller);
        sellerTRY.setAssetName("TRY");
        sellerTRY.setSize(new BigDecimal("10000"));
        sellerTRY.setUsableSize(new BigDecimal("10000"));

        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "BTC")).thenReturn(Optional.of(buyerBTC));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "BTC")).thenReturn(Optional.of(sellerBTC));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "TRY")).thenReturn(Optional.of(buyerTRY));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "TRY")).thenReturn(Optional.of(sellerTRY));

        OrderDto buyOrderDto = new OrderDto();
        OrderDto sellOrderDto = new OrderDto();
        when(orderConverter.convertToDTO(any(Order.class))).thenReturn(buyOrderDto, sellOrderDto);

        // Act
        List<OrderDto> result = orderService.matchPendingOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(assetRepository, times(4)).save(any(Asset.class));
        assertEquals(OrderStatus.MATCHED, sellOrder.getStatus());
        assertEquals(new BigDecimal("1.0"), buyOrder.getSize());
    }

}

