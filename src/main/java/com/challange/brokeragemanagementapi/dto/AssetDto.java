package com.challange.brokeragemanagementapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AssetDto {

        private Long id;
        private Long customerId;
        private String assetName;
        private BigDecimal size;
        private BigDecimal usableSize;
}
