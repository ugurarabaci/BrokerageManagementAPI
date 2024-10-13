package com.challange.brokeragemanagementapi.dto;

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
public class OrderDto {

    private Long id;
    private Long customerId;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
