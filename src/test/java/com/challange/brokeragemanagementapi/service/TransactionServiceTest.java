package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Transaction;
import com.challange.brokeragemanagementapi.model.enumtype.TransactionType;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import com.challange.brokeragemanagementapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void recordDeposit_ShouldCreateAndSaveDepositTransaction() {
        Long customerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(1000);
        String assetName = "TRY";

        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        transactionService.recordDeposit(customerId, amount, assetName);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(customer, savedTransaction.getCustomer());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(TransactionType.DEPOSIT, savedTransaction.getTransactionType());
        assertEquals(assetName, savedTransaction.getAssetName());
        assertNotNull(savedTransaction.getTransactionDate());
    }

    @Test
    void recordWithdrawal_ShouldCreateAndSaveWithdrawalTransaction() {
        Long customerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(500);
        String assetName = "TRY";
        String iban = "TR123456789";

        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        transactionService.recordWithdrawal(customerId, amount, assetName, iban);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(customer, savedTransaction.getCustomer());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(TransactionType.WITHDRAW, savedTransaction.getTransactionType());
        assertEquals(assetName, savedTransaction.getAssetName());
        assertEquals(iban, savedTransaction.getIban());
        assertNotNull(savedTransaction.getTransactionDate());
    }

}
