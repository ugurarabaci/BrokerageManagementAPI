package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.mapper.AssetConverter;
import com.challange.brokeragemanagementapi.model.enumtype.ResponseStatusType;
import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.challange.brokeragemanagementapi.model.response.AssetListResponse;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import com.challange.brokeragemanagementapi.service.AssetService;
import com.challange.brokeragemanagementapi.service.TransactionService;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public AssetListResponse listAssetsByCustomerId(Long customerId) {
        try {
            List<AssetDto> assets = assetService.listAssets(customerId);
            AssetListResponse assetResponses = new AssetListResponse();
            assetResponses.setStatus(ResponseStatusType.SUCCESS.getValue());
            assetResponses.setAssetDtoList(assets);
            return assetResponses;
        } catch (CustomerNotFoundException e) {
            AssetListResponse response = new AssetListResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("Failed to list assets: " + e.getMessage());
            return response;
        } catch (Exception e) {
            AssetListResponse response = new AssetListResponse();
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setErrorMessage("An unexpected error occurred while listing assets");
            return response;
        }
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
