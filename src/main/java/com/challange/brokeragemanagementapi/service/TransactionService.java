package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.model.Transaction;
import com.challange.brokeragemanagementapi.model.enumtype.TransactionType;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public TransactionService(TransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    public void recordDeposit(Long customerId, BigDecimal amount, String assetName) {
        Transaction transaction = new Transaction();
        transaction.setCustomer(customerRepository.findById(customerId).get());
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAssetName(assetName);
        transactionRepository.save(transaction);
    }

    public void recordWithdrawal(Long customerId, BigDecimal amount, String assetName, String iban) {
        Transaction transaction = new Transaction();
        transaction.setCustomer(customerRepository.findById(customerId).get());
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setAssetName(assetName);
        transaction.setIban(iban);
        transaction.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}