package com.challange.brokeragemanagementapi.security;

import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public SecurityService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName().equals("admin");
    }

    public boolean isOwner(Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        log.info("isOwner check: currentUsername={}, requestedCustomerId={}", currentUsername, customerId);

        boolean isOwner = customerRepository.findById(customerId)
                .map(customer -> customer.getUsername().equals(currentUsername))
                .orElse(false);

        log.info("isOwner result: {}", isOwner);

        return isOwner;
    }

    public boolean isOwnerOfOrder(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return orderRepository.findById(orderId)
                .map(order -> order.getCustomer().getUsername().equals(currentUsername))
                .orElse(false);
    }
}
