package com.challange.brokeragemanagementapi.security;

import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public SecurityService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public boolean isOwner(Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Burada, currentUsername'in customerId ile eşleşip eşleşmediğini kontrol etmelisiniz
        // Bu, veritabanınızdaki kullanıcı-müşteri ilişkisine bağlı olarak değişebilir

        // Örnek bir kontrol:
        return customerRepository.findById(customerId)
                .map(customer -> customer.getUsername().equals(currentUsername))
                .orElse(false);
    }

    public boolean isOwnerOfOrder(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Burada, currentUsername'in orderId'ye sahip siparişin sahibi olup olmadığını kontrol etmelisiniz

        // Örnek bir kontrol:
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomer().getUsername().equals(currentUsername))
                .orElse(false);
    }
}
