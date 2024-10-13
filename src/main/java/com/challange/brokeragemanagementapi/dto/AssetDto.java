package com.challange.brokeragemanagementapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDto {

        private Long id;
        private Long customerId;
        private String assetName;
        private Double size;
        private Double usableSize;
}
