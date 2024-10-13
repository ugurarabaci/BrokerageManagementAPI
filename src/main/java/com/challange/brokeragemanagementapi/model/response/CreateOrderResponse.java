package com.challange.brokeragemanagementapi.model.response;

import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@RequiredArgsConstructor
@Getter
@Setter
public class CreateOrderResponse {
    private Long id;
    private Long customerId;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createDate;

}
