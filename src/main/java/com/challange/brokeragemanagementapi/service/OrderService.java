package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import com.challange.brokeragemanagementapi.exception.AssetNotFoundException;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.exception.InvalidOrderStatusException;
import com.challange.brokeragemanagementapi.exception.OrderNotFoundException;
import com.challange.brokeragemanagementapi.mapper.OrderConverter;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import com.challange.brokeragemanagementapi.repository.AssetRepository;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final OrderConverter orderConverter;

    public OrderService(OrderRepository orderRepository, AssetService assetService,
                        CustomerRepository customerRepository, AssetRepository assetRepository, OrderConverter orderConverter) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
        this.customerRepository = customerRepository;
        this.assetRepository = assetRepository;
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

    @Transactional
    public List<OrderDto> matchPendingOrders() {

        log.info("Starting to match pending orders");

        List<Order> buyOrders = orderRepository.findByOrderSideAndStatus(OrderSide.BUY, OrderStatus.PENDING);
        List<Order> sellOrders = orderRepository.findByOrderSideAndStatus(OrderSide.SELL, OrderStatus.PENDING);

        log.info("Found {} pending buy orders and {} pending sell orders", buyOrders.size(), sellOrders.size());

        List<OrderDto> matchedOrders = new ArrayList<>();

        for (Order buyOrder : buyOrders) {
            log.debug("Processing buy order: id={}, assetName={}, size={}, price={}",
                    buyOrder.getId(), buyOrder.getAssetName(), buyOrder.getSize(), buyOrder.getPrice());

            for (Order sellOrder : sellOrders) {
                log.debug("Comparing with sell order: id={}, assetName={}, size={}, price={}",
                        sellOrder.getId(), sellOrder.getAssetName(), sellOrder.getSize(), sellOrder.getPrice());

                if (buyOrder.getAssetName().equals(sellOrder.getAssetName()) &&
                        buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0) {

                    BigDecimal matchedQuantity = buyOrder.getSize().min(sellOrder.getSize());
                    log.info("Match found: buyOrderId={}, sellOrderId={}, assetName={}, matchedQuantity={}, price={}",
                            buyOrder.getId(), sellOrder.getId(), buyOrder.getAssetName(), matchedQuantity, sellOrder.getPrice());

                    try {
                        buyOrder.setSize(buyOrder.getSize().subtract(matchedQuantity));
                        if (buyOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
                            buyOrder.setStatus(OrderStatus.MATCHED);
                            log.info("Buy order fully matched: id={}", buyOrder.getId());
                        }

                        sellOrder.setSize(sellOrder.getSize().subtract(matchedQuantity));
                        if (sellOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
                            sellOrder.setStatus(OrderStatus.MATCHED);
                            log.info("Sell order fully matched: id={}", sellOrder.getId());
                        }

                        updateAssets(buyOrder.getCustomer(), sellOrder.getCustomer(),
                                buyOrder.getAssetName(), matchedQuantity, sellOrder.getPrice());

                        orderRepository.save(buyOrder);
                        orderRepository.save(sellOrder);

                        matchedOrders.add(orderConverter.convertToDTO(buyOrder));
                        matchedOrders.add(orderConverter.convertToDTO(sellOrder));

                        log.info("Order match processed successfully");
                    } catch (Exception e) {
                        log.error("Error processing order match: buyOrderId={}, sellOrderId={}, error={}",
                                buyOrder.getId(), sellOrder.getId(), e.getMessage(), e);
                    }

                    if (buyOrder.getStatus() == OrderStatus.MATCHED) {
                        log.debug("Buy order fully matched, moving to next buy order");
                        break;
                    }
                }
            }
        }

        log.info("Matching process completed. Total matched orders: {}", matchedOrders.size());
        return matchedOrders;
    }

    private void updateAssets(Customer buyer, Customer seller, String assetName,
                              BigDecimal quantity, BigDecimal price) {
        log.info("Updating assets for matched order: asset={}, quantity={}, price={}", assetName, quantity, price);

        Asset buyerAsset = assetRepository.findByCustomerIdAndAssetName(buyer.getId(), assetName)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found for buyer: " + assetName));
        buyerAsset.setSize(buyerAsset.getSize().add(quantity));
        buyerAsset.setUsableSize(buyerAsset.getUsableSize().add(quantity));
        assetRepository.save(buyerAsset);

        log.info("Updated buyer's asset: customerId={}, assetName={}, newSize={}, newUsableSize={}",
                buyer.getId(), assetName, buyerAsset.getSize(), buyerAsset.getUsableSize());

        Asset sellerAsset = assetRepository.findByCustomerIdAndAssetName(seller.getId(), assetName)
                .orElseThrow(() -> {
                    log.error("Asset not found for seller: customerId={}, assetName={}", seller.getId(), assetName);
                    return new AssetNotFoundException("Asset not found for seller: " + assetName);
                });
        sellerAsset.setSize(sellerAsset.getSize().subtract(quantity));
        sellerAsset.setUsableSize(sellerAsset.getUsableSize().subtract(quantity));
        assetRepository.save(sellerAsset);

        log.info("Updated seller's asset: customerId={}, assetName={}, newSize={}, newUsableSize={}",
                seller.getId(), assetName, sellerAsset.getSize(), sellerAsset.getUsableSize());


        BigDecimal totalPrice = price.multiply(quantity);
        updateTRYAsset(buyer, totalPrice.negate());
        updateTRYAsset(seller, totalPrice);
        log.info("Asset update completed for matched order");
    }

    private void updateTRYAsset(Customer customer, BigDecimal amount) {
        log.info("Updating TRY asset for customer: customerId={}, amount={}", customer.getId(), amount);
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")
                .orElseThrow(() -> {
                    log.error("TRY asset not found for customer: customerId={}", customer.getId());
                    return new AssetNotFoundException("TRY asset not found for customer: " + customer.getId());
                });
        tryAsset.setSize(tryAsset.getSize().add(amount));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(amount));
        assetRepository.save(tryAsset);
        log.info("Updated TRY asset: customerId={}, newSize={}, newUsableSize={}",
                customer.getId(), tryAsset.getSize(), tryAsset.getUsableSize());

    }
}