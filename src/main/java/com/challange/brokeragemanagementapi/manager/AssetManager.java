package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.exception.AssetNotFoundException;
import com.challange.brokeragemanagementapi.exception.CustomerNotFoundException;
import com.challange.brokeragemanagementapi.exception.InsufficientFundsException;
import com.challange.brokeragemanagementapi.exception.InvalidIbanException;
import com.challange.brokeragemanagementapi.converter.AssetConverter;
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
        AssetListResponse response = new AssetListResponse();
        try {
            List<AssetDto> assets = assetService.listAssets(customerId);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());
            response.setAssetDtoList(assets);
            return response;
        } catch (CustomerNotFoundException e) {
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setMessage("Failed to list assets: " + e.getMessage());
            return response;
        } catch (Exception e) {
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setMessage("An unexpected error occurred while listing assets");
            return response;
        }
    }

    public AssetResponse depositMoney(Long customerId, DepositRequest depositRequest) {
        AssetResponse response;
        AssetDto updatedAsset = assetConverter.convertToDTO(assetService.depositMoney(customerId, depositRequest.getAmount()), customerId);
        transactionService.recordDeposit(customerId, depositRequest.getAmount(), "TRY");
        response = assetConverter.convertToResponse(updatedAsset);
        response.setStatus("SUCCESS");
        response.setMessage("Deposit successful for customer: " + customerId);
        return response;
    }

    public AssetResponse withdrawMoney(Long customerId, WithdrawRequest withdrawRequest) {
        AssetResponse response = new AssetResponse();

        try {
            if (withdrawRequest.getIban() == null || withdrawRequest.getIban().length() < 15) {
                throw new InvalidIbanException("IBAN must be at least 15 characters long");
            }
            AssetDto updatedAsset = assetConverter.convertToDTO(
                    assetService.withdrawMoney(customerId, withdrawRequest.getAmount(), withdrawRequest.getIban()),
                    customerId
            );
            transactionService.recordWithdrawal(customerId, withdrawRequest.getAmount(), "TRY", withdrawRequest.getIban());

            response = assetConverter.convertToResponse(updatedAsset);
            response.setStatus(ResponseStatusType.SUCCESS.getValue());
            response.setMessage("Withdrawal successful for customer: " + customerId);
        } catch (InvalidIbanException | AssetNotFoundException | InsufficientFundsException |
                 CustomerNotFoundException e) {
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatus(ResponseStatusType.FAILURE.getValue());
            response.setMessage("An unexpected error occurred: " + e.getMessage());
        }

        return response;
    }

}
