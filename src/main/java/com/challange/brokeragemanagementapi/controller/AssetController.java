package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.manager.AssetManager;
import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.challange.brokeragemanagementapi.model.response.AssetListResponse;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@Tag(name = "Asset Management", description = "For managing assets")
public class AssetController {
    private final AssetManager assetManager;

    public AssetController(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Operation(summary = "List Assets", description = "List all assets for a given customer")
    @ApiResponse(responseCode = "200", description = "Assets retrieved successfully",
            content = @Content(schema = @Schema(implementation = AssetListResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid customer ID")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@securityService.isOwnerOrAdmin(#customerId)")
    public ResponseEntity<AssetListResponse> listAssets(@PathVariable Long customerId) {
        AssetListResponse assetResponse = assetManager.listAssetsByCustomerId(customerId);
        return ResponseEntity.ok(assetResponse);
    }

    @Operation(summary = "Deposit Money", description = "Deposit money into a customer's account")
    @ApiResponse(responseCode = "200", description = "Money deposited successfully",
            content = @Content(schema = @Schema(implementation = AssetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid deposit request")
    @PostMapping("/deposit/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    public ResponseEntity<AssetResponse> depositMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody DepositRequest depositRequest) {
        AssetResponse assetResponse = assetManager.depositMoney(customerId, depositRequest);
        return ResponseEntity.ok(assetResponse);
    }

    @Operation(summary = "Withdraw Money", description = "Withdraw money from a customer")
    @ApiResponse(responseCode = "200", description = "Money withdrawn successfully",
            content = @Content(schema = @Schema(implementation = AssetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid withdrawal request")
    @PostMapping("/withdraw/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    public ResponseEntity<AssetResponse> withdrawMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody WithdrawRequest withdrawRequest) {
        AssetResponse assetResponse = assetManager.withdrawMoney(customerId, withdrawRequest);
        return ResponseEntity.ok(assetResponse);
    }
}
