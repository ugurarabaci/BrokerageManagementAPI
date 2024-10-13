package com.challange.brokeragemanagementapi.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Response {
    private String status;
    private String errorMessage;
}
