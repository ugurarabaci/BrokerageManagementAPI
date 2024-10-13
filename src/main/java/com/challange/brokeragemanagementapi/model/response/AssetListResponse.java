package com.challange.brokeragemanagementapi.model.response;

import com.challange.brokeragemanagementapi.dto.AssetDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class AssetListResponse extends Response{

    private List<AssetDto> assetDtoList;
}
