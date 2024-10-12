package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}