package com.challange.brokeragemanagementapi.mapper;

import com.challange.brokeragemanagementapi.model.enumtype.ResponseStatusType;
import com.challange.brokeragemanagementapi.model.response.Response;
import org.springframework.stereotype.Component;

@Component
public class ResponseConverter {

    public Response mapToResponse(Response response) {
        response.setStatus(ResponseStatusType.SUCCESS.getValue());
        return response;
    }

    public Response prepareSuccessfulResponse() {
        Response response = new Response();
        response.setStatus(ResponseStatusType.SUCCESS.getValue());
        return response;
    }

    public Response prepareFailureResponse(String errorMessage) {
        Response response = new Response();
        response.setStatus(ResponseStatusType.FAILURE.getValue());
        response.setErrorMessage(errorMessage);
        return response;
    }

}
