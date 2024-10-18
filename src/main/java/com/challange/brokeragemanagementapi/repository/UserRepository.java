package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserRole, Long> {
}