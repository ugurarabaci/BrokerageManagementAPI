package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findById(Long id);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
}