package com.challange.brokeragemanagementapi.manager;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.mapper.AssetConverter;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import com.challange.brokeragemanagementapi.service.AssetService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssetManager {

    private final AssetService assetService;
    private final AssetConverter assetConverter;

    public AssetManager(AssetService assetService, AssetConverter assetConverter) {
        this.assetService = assetService;
        this.assetConverter = assetConverter;
    }

    public List<AssetResponse> listAssetsByCustomerId(Long customerId) {
        List<AssetDto> assets = assetService.listAssets(customerId);
        return assets.stream()
                .map(assetConverter::convertToResponse)
                .collect(Collectors.toList());
    }
}
