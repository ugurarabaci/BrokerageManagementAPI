package com.challange.brokeragemanagementapi.mapper;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import com.challange.brokeragemanagementapi.model.Asset;
import com.challange.brokeragemanagementapi.model.response.AssetResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetConverter {
    public static AssetDto convertToDTO(Asset asset) {
        AssetDto assetDTO = new AssetDto();
        assetDTO.setId(asset.getId());
        //assetDTO.setCustomerId(asset.getCustomerId());
        assetDTO.setAssetName(asset.getAssetName());
        assetDTO.setSize(asset.getSize().doubleValue());
        assetDTO.setUsableSize(asset.getUsableSize().doubleValue());
        return assetDTO;
    }

    public static Asset convertToEntity(AssetDto assetDTO) {
        Asset asset = new Asset();
        asset.setId(assetDTO.getId());
        //asset.setCustomer(assetDTO.getCustomerId());
        asset.setAssetName(assetDTO.getAssetName());
        asset.setSize(BigDecimal.valueOf(assetDTO.getSize()));
        asset.setUsableSize(BigDecimal.valueOf(assetDTO.getUsableSize()));
        return asset;
    }

    public AssetResponse convertToResponse(AssetDto assetDTO) {
        AssetResponse assetResponse = new AssetResponse();
        assetResponse.setId(assetDTO.getId());
        assetResponse.setCustomerId(assetDTO.getCustomerId());
        assetResponse.setAssetName(assetDTO.getAssetName());
        assetResponse.setSize(BigDecimal.valueOf(assetDTO.getSize()));
        assetResponse.setUsableSize(BigDecimal.valueOf(assetDTO.getUsableSize()));
        return assetResponse;
    }
}
