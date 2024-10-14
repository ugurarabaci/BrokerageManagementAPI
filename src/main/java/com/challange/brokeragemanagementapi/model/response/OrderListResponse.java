package com.challange.brokeragemanagementapi.model.response;

import com.challange.brokeragemanagementapi.dto.OrderDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderListResponse extends Response{
    private List<OrderDto> orderDtoList;
}
