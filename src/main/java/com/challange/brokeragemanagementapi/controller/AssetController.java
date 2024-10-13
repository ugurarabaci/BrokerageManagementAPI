package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.manager.AssetManager;
import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.challange.brokeragemanagementapi.model.response.AssetListResponse;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetManager assetManager;

    public AssetController(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@securityService.isOwnerOrAdmin(#customerId)")
    public ResponseEntity<AssetListResponse> listAssets(@PathVariable Long customerId) {
        AssetListResponse assetResponse = assetManager.listAssetsByCustomerId(customerId);
        return ResponseEntity.ok(assetResponse);
    }

    @PostMapping("/deposit/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    public ResponseEntity<AssetResponse> depositMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody DepositRequest depositRequest) {
        AssetResponse assetResponse = assetManager.depositMoney(customerId, depositRequest);
        return ResponseEntity.ok(assetResponse);
    }

    @PostMapping("/withdraw/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    public ResponseEntity<AssetResponse> withdrawMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody WithdrawRequest withdrawRequest) {
        AssetResponse assetResponse = assetManager.withdrawMoney(customerId, withdrawRequest);
        return ResponseEntity.ok(assetResponse);
    }
}
