package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.manager.AssetManager;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetManager assetManager;

    public AssetController(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@securityService.isOwnerOrAdmin(#customerId)")
    public ResponseEntity<List<AssetResponse>> listAssets(@PathVariable Long customerId) {
        List<AssetResponse> assets = assetManager.listAssetsByCustomerId(customerId);
        return ResponseEntity.ok(assets);
    }
}
