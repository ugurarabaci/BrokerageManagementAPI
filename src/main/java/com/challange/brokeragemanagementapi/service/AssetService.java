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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    public AssetService(AssetRepository assetRepository, CustomerRepository customerRepository) {
        this.assetRepository = assetRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void validateAndUpdateAssetForOrder(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            validateAndUpdateForBuyOrder(order);
        } else {
            validateAndUpdateForSellOrder(order);
        }
    }

    private void validateAndUpdateForBuyOrder(Order order) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), "TRY")
                .orElseThrow(() -> new IllegalStateException("TRY asset not found for customer"));

        BigDecimal requiredAmount = order.getSize().multiply(order.getPrice());
        if (tryAsset.getUsableSize().compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException("Insufficient TRY balance for buy order");
        }

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(requiredAmount));
        assetRepository.save(tryAsset);
    }

    @Transactional
    public void revertAssetForDeletedOrder(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            revertAssetForDeletedBuyOrder(order);
        } else {
            revertAssetForDeletedSellOrder(order);
        }
    }

    private void revertAssetForDeletedBuyOrder(Order order) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), "TRY")
                .orElseThrow(() -> new AssetNotFoundException("TRY asset not found for customer"));

        BigDecimal amountToRevert = order.getSize().multiply(order.getPrice());
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(amountToRevert));
        assetRepository.save(tryAsset);
    }

    private void validateAndUpdateForSellOrder(Order order) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                .orElseThrow(() -> new IllegalStateException("Asset not found for customer"));

        if (asset.getUsableSize().compareTo(order.getSize()) < 0) {
            throw new IllegalStateException("Insufficient asset balance for sell order");
        }

        asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        assetRepository.save(asset);
    }

    private void revertAssetForDeletedSellOrder(Order order) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                .orElseThrow(() -> new AssetNotFoundException("Asset not found for customer"));

        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        assetRepository.save(asset);
    }

    public List<AssetDto> listAssets(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return assetRepository.findByCustomerId(customerId).stream()
                .map(asset -> {
                    AssetDto assetDto = new AssetDto();
                    assetDto.setId(asset.getId());
                    assetDto.setCustomerId(asset.getCustomer().getId());
                    assetDto.setAssetName(asset.getAssetName());
                    assetDto.setSize(asset.getSize());
                    assetDto.setUsableSize(asset.getUsableSize());
                    return assetDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Asset depositMoney(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")
                .orElseGet(() -> {
                    Asset newAsset = new Asset();
                    newAsset.setCustomer(customer);
                    newAsset.setAssetName("TRY");
                    newAsset.setSize(BigDecimal.ZERO);
                    newAsset.setUsableSize(BigDecimal.ZERO);
                    return newAsset;
                });

        tryAsset.setSize(tryAsset.getSize().add(amount));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(amount));

        return assetRepository.save(tryAsset);
    }

    @Transactional
    public Asset withdrawMoney(Long customerId, BigDecimal amount, String iban) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")
                .orElseThrow(() -> new AssetNotFoundException("TRY asset not found for customer: " + customerId));

        if (tryAsset.getUsableSize().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        tryAsset.setSize(tryAsset.getSize().subtract(amount));
        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(amount));

        return assetRepository.save(tryAsset);
    }

}
