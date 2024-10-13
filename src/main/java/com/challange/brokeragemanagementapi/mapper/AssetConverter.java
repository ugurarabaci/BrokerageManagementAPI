package com.challange.brokeragemanagementapi.mapper;

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

    public static Asset convertToEntity(AssetDto assetDTO) {
        Asset asset = new Asset();
        asset.setId(assetDTO.getId());
        //asset.setCustomer(assetDTO.getCustomerId());
        asset.setAssetName(assetDTO.getAssetName());
        asset.setSize((assetDTO.getSize()));
        asset.setUsableSize((assetDTO.getUsableSize()));
        return asset;
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
    public AssetDto convertToDTO(AssetResponse assetResponse) {
        AssetDto assetDTO = new AssetDto();
        assetDTO.setId(assetResponse.getId());
        assetDTO.setCustomerId(assetResponse.getCustomerId());
        assetDTO.setAssetName(assetResponse.getAssetName());
        assetDTO.setSize(assetResponse.getSize());
        assetDTO.setUsableSize(assetResponse.getUsableSize());
        return assetDTO;
    }
}
