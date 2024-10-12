package com.challange.brokeragemanagementapi.model.request;

import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private String assetName;

    @NotNull
    private OrderSide orderSide;

    @NotNull
    @Positive
    private BigDecimal size;

    @NotNull
    @Positive
    private BigDecimal price;

}