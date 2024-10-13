package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.exception.AssetNotFoundException;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.exception.InsufficientFundsException;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.Customer;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.repository.AssetRepository;
import com.challange.brokeragemanagementapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void should_deposit_money() {
        // Arrange
        Long customerId = 1L;
        BigDecimal depositAmount = new BigDecimal("100.00");
        Customer customer = new Customer();
        customer.setId(customerId);

        Asset existingAsset = new Asset();
        existingAsset.setCustomer(customer);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("500.00"));
        existingAsset.setUsableSize(new BigDecimal("500.00"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Asset result = assetService.depositMoney(customerId, depositAmount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("600.00"), result.getSize());
        assertEquals(new BigDecimal("600.00"), result.getUsableSize());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void should_deposit_money_new_asset() {
        // Arrange
        Long customerId = 1L;
        BigDecimal depositAmount = new BigDecimal("100.00");
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Asset result = assetService.depositMoney(customerId, depositAmount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getSize());
        assertEquals(new BigDecimal("100.00"), result.getUsableSize());
        assertEquals("TRY", result.getAssetName());
        assertEquals(customer, result.getCustomer());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void should_deposit_money_when_customer_not_found() {
        // Arrange
        Long customerId = 1L;
        BigDecimal depositAmount = new BigDecimal("100.00");

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> assetService.depositMoney(customerId, depositAmount));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void should_withdraw_money_success() {
        // Arrange
        Long customerId = 1L;
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        String iban = "TR330006100519786457841326";
        Customer customer = new Customer();
        customer.setId(customerId);

        Asset existingAsset = new Asset();
        existingAsset.setCustomer(customer);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("500.00"));
        existingAsset.setUsableSize(new BigDecimal("500.00"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Asset result = assetService.withdrawMoney(customerId, withdrawAmount, iban);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("400.00"), result.getSize());
        assertEquals(new BigDecimal("400.00"), result.getUsableSize());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void should_withdraw_money_if_insufficient_funds() {
        // Arrange
        Long customerId = 1L;
        BigDecimal withdrawAmount = new BigDecimal("600.00");
        String iban = "TR330006100519786457841326";
        Customer customer = new Customer();
        customer.setId(customerId);

        Asset existingAsset = new Asset();
        existingAsset.setCustomer(customer);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("500.00"));
        existingAsset.setUsableSize(new BigDecimal("500.00"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.of(existingAsset));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> assetService.withdrawMoney(customerId, withdrawAmount, iban));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void should_withdraw_money_if_asset_not_found() {
        // Arrange
        Long customerId = 1L;
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        String iban = "TR330006100519786457841326";
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AssetNotFoundException.class, () -> assetService.withdrawMoney(customerId, withdrawAmount, iban));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void validateAndUpdateAssetForOrder_BuyOrder_ShouldUpdateTRYAsset() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(BigDecimal.valueOf(100000));
        tryAsset.setUsableSize(BigDecimal.valueOf(100000));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(java.util.Optional.of(tryAsset));

        // Act
        assetService.validateAndUpdateAssetForOrder(order);

        // Assert
        assertEquals(BigDecimal.valueOf(100000), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(50000), tryAsset.getUsableSize());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void validateAndUpdateAssetForOrder_BuyOrder_InsufficientFunds_ShouldThrowException() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(BigDecimal.valueOf(40000));
        tryAsset.setUsableSize(BigDecimal.valueOf(40000));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(java.util.Optional.of(tryAsset));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> assetService.validateAndUpdateAssetForOrder(order));
    }

    @Test
    void validateAndUpdateAssetForOrder_SellOrder_ShouldUpdateAsset() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        Asset btcAsset = new Asset();
        btcAsset.setAssetName("BTC");
        btcAsset.setSize(BigDecimal.valueOf(2));
        btcAsset.setUsableSize(BigDecimal.valueOf(2));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "BTC"))
                .thenReturn(java.util.Optional.of(btcAsset));

        // Act
        assetService.validateAndUpdateAssetForOrder(order);

        // Assert
        assertEquals(BigDecimal.valueOf(2), btcAsset.getSize());
        assertEquals(BigDecimal.ONE, btcAsset.getUsableSize());
        verify(assetRepository).save(btcAsset);
    }

    @Test
    void validateAndUpdateAssetForOrder_SellOrder_InsufficientAssets_ShouldThrowException() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.valueOf(2));
        order.setPrice(BigDecimal.valueOf(50000));

        Asset btcAsset = new Asset();
        btcAsset.setAssetName("BTC");
        btcAsset.setSize(BigDecimal.ONE);
        btcAsset.setUsableSize(BigDecimal.ONE);

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "BTC"))
                .thenReturn(java.util.Optional.of(btcAsset));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> assetService.validateAndUpdateAssetForOrder(order));
    }

    @Test
    void revertAssetForDeletedOrder_BuyOrder_ShouldRevertTRYAsset() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(BigDecimal.valueOf(100000));
        tryAsset.setUsableSize(BigDecimal.valueOf(50000));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(tryAsset));

        // Act
        assetService.revertAssetForDeletedOrder(order);

        // Assert
        assertEquals(BigDecimal.valueOf(100000), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(100000), tryAsset.getUsableSize());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void revertAssetForDeletedOrder_SellOrder_ShouldRevertAsset() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.valueOf(1.5));
        order.setPrice(BigDecimal.valueOf(50000));

        Asset btcAsset = new Asset();
        btcAsset.setAssetName("BTC");
        btcAsset.setSize(BigDecimal.valueOf(2));
        btcAsset.setUsableSize(BigDecimal.valueOf(0.5));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "BTC"))
                .thenReturn(Optional.of(btcAsset));

        // Act
        assetService.revertAssetForDeletedOrder(order);

        // Assert
        assertEquals(BigDecimal.valueOf(2), btcAsset.getSize());
        verify(assetRepository).save(btcAsset);
    }

    @Test
    void revertAssetForDeletedOrder_BuyOrder_AssetNotFound_ShouldThrowException() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AssetNotFoundException.class, () -> assetService.revertAssetForDeletedOrder(order));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void revertAssetForDeletedOrder_SellOrder_AssetNotFound_ShouldThrowException() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.valueOf(50000));

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "BTC"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AssetNotFoundException.class, () -> assetService.revertAssetForDeletedOrder(order));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void revertAssetForDeletedOrder_BuyOrder_PartialRevert_ShouldUpdateAsset() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("BTC");
        order.setSize(BigDecimal.valueOf(1.5));
        order.setPrice(BigDecimal.valueOf(50000));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(BigDecimal.valueOf(100000));
        tryAsset.setUsableSize(BigDecimal.valueOf(25000)); // Partially used

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(tryAsset));

        // Act
        assetService.revertAssetForDeletedOrder(order);

        // Assert
        assertEquals(BigDecimal.valueOf(100000), tryAsset.getSize());
        verify(assetRepository).save(tryAsset);
    }
    @Test
    void listAssets_CustomerExists_WithAssets_ShouldReturnAssetDtoList() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);

        Asset asset1 = new Asset();
        asset1.setId(1L);
        asset1.setCustomer(customer);
        asset1.setAssetName("BTC");
        asset1.setSize(BigDecimal.valueOf(1.5));
        asset1.setUsableSize(BigDecimal.valueOf(1.0));

        Asset asset2 = new Asset();
        asset2.setId(2L);
        asset2.setCustomer(customer);
        asset2.setAssetName("ETH");
        asset2.setSize(BigDecimal.valueOf(10.0));
        asset2.setUsableSize(BigDecimal.valueOf(8.0));

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(assetRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList(asset1, asset2));

        // Act
        List<AssetDto> result = assetService.listAssets(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        AssetDto assetDto1 = result.get(0);
        assertEquals(1L, assetDto1.getId());
        assertEquals(customerId, assetDto1.getCustomerId());
        assertEquals("BTC", assetDto1.getAssetName());
        assertEquals(BigDecimal.valueOf(1.5), assetDto1.getSize());
        assertEquals(BigDecimal.valueOf(1.0), assetDto1.getUsableSize());

        AssetDto assetDto2 = result.get(1);
        assertEquals(2L, assetDto2.getId());
        assertEquals(customerId, assetDto2.getCustomerId());
        assertEquals("ETH", assetDto2.getAssetName());
        assertEquals(BigDecimal.valueOf(10.0), assetDto2.getSize());
        assertEquals(BigDecimal.valueOf(8.0), assetDto2.getUsableSize());

        verify(customerRepository).existsById(customerId);
        verify(assetRepository).findByCustomerId(customerId);
    }

    @Test
    void listAssets_CustomerExists_NoAssets_ShouldReturnEmptyList() {
        // Arrange
        Long customerId = 1L;

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(assetRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // Act
        List<AssetDto> result = assetService.listAssets(customerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(customerRepository).existsById(customerId);
        verify(assetRepository).findByCustomerId(customerId);
    }

    @Test
    void listAssets_CustomerDoesNotExist_ShouldThrowCustomerNotFoundException() {
        // Arrange
        Long customerId = 1L;

        when(customerRepository.existsById(customerId)).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> assetService.listAssets(customerId));

        verify(customerRepository).existsById(customerId);
        verifyNoInteractions(assetRepository);
    }

}