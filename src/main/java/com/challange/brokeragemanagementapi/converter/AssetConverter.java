package com.challange.brokeragemanagementapi.converter;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import org.springframework.stereotype.Component;

@Component
public class AssetConverter {
    public AssetDto convertToDTO(Asset asset, Long customerId) {
        AssetDto assetDTO = new AssetDto();
        assetDTO.setId(asset.getId());
        assetDTO.setCustomerId(customerId);
        assetDTO.setAssetName(asset.getAssetName());
        assetDTO.setSize(asset.getSize());
        assetDTO.setUsableSize(asset.getUsableSize());
        return assetDTO;
    }

    public AssetResponse convertToResponse(AssetDto assetDTO) {
        AssetResponse assetResponse = new AssetResponse();
        assetResponse.setId(assetDTO.getId());
        assetResponse.setCustomerId(assetDTO.getCustomerId());
        assetResponse.setAssetName(assetDTO.getAssetName());
        assetResponse.setSize((assetDTO.getSize()));
        assetResponse.setUsableSize((assetDTO.getUsableSize()));
        return assetResponse;
    }
}
