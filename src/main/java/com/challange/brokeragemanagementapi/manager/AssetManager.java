package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.mapper.AssetConverter;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import com.challange.brokeragemanagementapi.service.AssetService;
import com.challange.brokeragemanagementapi.service.TransactionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssetManager {

    private final AssetService assetService;
    private final AssetConverter assetConverter;

    private final TransactionService transactionService;

    public AssetManager(AssetService assetService, AssetConverter assetConverter, TransactionService transactionService) {
        this.assetService = assetService;
        this.assetConverter = assetConverter;
        this.transactionService = transactionService;
    }

    public List<AssetResponse> listAssetsByCustomerId(Long customerId) {
        List<AssetDto> assets = assetService.listAssets(customerId);
        return assets.stream()
                .map(assetConverter::convertToResponse)
                .collect(Collectors.toList());
    }

    public AssetResponse depositMoney(Long customerId, DepositRequest depositRequest) {
        AssetDto updatedAsset = assetConverter.convertToDTO(assetService.depositMoney(customerId, depositRequest.getAmount()), customerId);
        transactionService.recordDeposit(customerId, depositRequest.getAmount(), "TRY");
        return assetConverter.convertToResponse(updatedAsset);
    }

    public AssetResponse withdrawMoney(Long customerId, WithdrawRequest withdrawRequest) {
        AssetDto updatedAsset = assetConverter.convertToDTO(assetService.withdrawMoney(customerId, withdrawRequest.getAmount(), withdrawRequest.getIban()), customerId);
        transactionService.recordWithdrawal(customerId, withdrawRequest.getAmount(), "TRY", withdrawRequest.getIban());
        return assetConverter.convertToResponse(updatedAsset);
    }

}
