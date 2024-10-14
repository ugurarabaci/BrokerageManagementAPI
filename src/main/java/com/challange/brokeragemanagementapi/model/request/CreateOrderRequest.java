package com.challange.brokeragemanagementapi.model.request;

import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    @Schema(description = "ID of the customer who is creating the order", example = "2")
    private Long customerId;

    @NotNull
    @Schema(description = "Name of the asset to be ordered", example = "BTC")
    private String assetName;

    @NotNull
    @Schema(description = "Side of the order (BUY or SELL)", example = "BUY")
    private OrderSide orderSide;

    @NotNull
    @Positive
    @Schema(description = "Size of the order", example = "10")
    private BigDecimal size;

    @NotNull
    @Positive
    @Schema(description = "Price of the order", example = "5000")
    private BigDecimal price;

}