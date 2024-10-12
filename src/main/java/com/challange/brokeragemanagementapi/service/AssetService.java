package com.challange.brokeragemanagementapi.service;

import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.Order;
import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.repository.AssetRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
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
            throw new IllegalStateException("Insufficient TRY balance for buy order");
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
                .orElseThrow(() -> new IllegalStateException("TRY asset not found for customer"));

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
                .orElseThrow(() -> new IllegalStateException("Asset not found for customer"));

        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        assetRepository.save(asset);
    }
}
