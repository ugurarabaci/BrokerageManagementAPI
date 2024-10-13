package com.challange.brokeragemanagementapi.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class AssetResponse {

    private Long id;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;

}
