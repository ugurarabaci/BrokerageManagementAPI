package com.challange.brokeragemanagementapi.model.enumtype;

public enum ResponseStatusType implements ValueEnum<String>{
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE");

    private final String value;

    ResponseStatusType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
