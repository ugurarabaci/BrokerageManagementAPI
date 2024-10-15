package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.exception.*;
import com.challange.brokeragemanagementapi.converter.AssetConverter;
import com.challange.brokeragemanagementapi.model.enumtype.ResponseStatusType;
import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.challange.brokeragemanagementapi.model.response.AssetListResponse;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import com.challange.brokeragemanagementapi.service.AssetService;
import com.challange.brokeragemanagementapi.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssetManager {

    private static final Logger log = LoggerFactory.getLogger(AssetManager.class);

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
            return createSuccessAssetListResponse(assets);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found while listing assets: {}", customerId);
            return createFailureAssetListResponse("Failed to list assets: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while listing assets for customer: {}", customerId, e);
            return createFailureAssetListResponse("An unexpected error occurred while listing assets");
        }
    }

    public AssetResponse depositMoney(Long customerId, DepositRequest depositRequest) {
        try {
            log.info("Deposit request received for customer: {}, amount: {}", customerId, depositRequest.getAmount());

            AssetDto updatedAsset = assetConverter.convertToDTO(
                    assetService.depositMoney(customerId, depositRequest.getAmount()),
                    customerId
            );
            transactionService.recordDeposit(customerId, depositRequest.getAmount(), "TRY");
            return createSuccessAssetResponse(updatedAsset, "Deposit successful for customer: " + customerId);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found while depositing money: {}", customerId);
            return createFailureAssetResponse("Customer not found: " + customerId);
        } catch (Exception e) {
            log.error("Unexpected error during deposit for customer: {}", customerId, e);
            return createFailureAssetResponse("An unexpected error occurred during deposit");
        }
    }

    public AssetResponse withdrawMoney(Long customerId, WithdrawRequest withdrawRequest) {
        try {
            validateIban(withdrawRequest.getIban());

            log.info("Withdraw request received for customer: {}, amount: {}, IBAN: {}",
                    customerId, withdrawRequest.getAmount(), withdrawRequest.getIban());

            AssetDto updatedAsset = assetConverter.convertToDTO(
                    assetService.withdrawMoney(customerId, withdrawRequest.getAmount(), withdrawRequest.getIban()),
                    customerId
            );
            transactionService.recordWithdrawal(customerId, withdrawRequest.getAmount(), "TRY", withdrawRequest.getIban());
            return createSuccessAssetResponse(updatedAsset, "Withdrawal successful for customer: " + customerId);
        } catch (InvalidIbanException e) {
            log.error("Invalid IBAN: {}", withdrawRequest.getIban());
            return createFailureAssetResponse(e.getMessage());
        } catch (AssetNotFoundException | InsufficientFundsException | CustomerNotFoundException e) {
            log.error("Error during withdrawal for customer: {}", customerId, e);
            return createFailureAssetResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during withdrawal for customer: {}", customerId, e);
            return createFailureAssetResponse("An unexpected error occurred during withdrawal");
        }
    }
    private AssetListResponse createSuccessAssetListResponse(List<AssetDto> assets) {
        AssetListResponse response = new AssetListResponse();
        response.setStatus(ResponseStatusType.SUCCESS.getValue());
        response.setAssetDtoList(assets);
        return response;
    }

    private AssetListResponse createFailureAssetListResponse(String message) {
        AssetListResponse response = new AssetListResponse();
        response.setStatus(ResponseStatusType.FAILURE.getValue());
        response.setMessage(message);
        return response;
    }

    private AssetResponse createSuccessAssetResponse(AssetDto assetDto, String message) {
        AssetResponse response = assetConverter.convertToResponse(assetDto);
        response.setStatus(ResponseStatusType.SUCCESS.getValue());
        response.setMessage(message);
        return response;
    }

    private AssetResponse createFailureAssetResponse(String message) {
        AssetResponse response = new AssetResponse();
        response.setStatus(ResponseStatusType.FAILURE.getValue());
        response.setMessage(message);
        return response;
    }

    private void validateIban(String iban) {
        if (iban == null || iban.length() < 15) {
            throw new InvalidIbanException("IBAN must be at least 15 characters long");
        }
    }
}
