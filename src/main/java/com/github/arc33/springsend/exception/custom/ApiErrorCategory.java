package com.github.arc33.springsend.exception.custom;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApiErrorCategory {
    API_ERROR("api_error"),
    CARD_ERROR("card_error"),
    IDEMPOTENCY_ERROR("idempotency_error"),
    INVALID_REQUEST_ERROR("invalid_request_error");

    private final String value;

    public String getValue() {
        return value;
    }
}