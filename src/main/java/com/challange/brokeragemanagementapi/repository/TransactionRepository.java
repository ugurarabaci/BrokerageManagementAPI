package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}