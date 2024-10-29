package com.github.arc33.springsend.exception.custom;

public class ApiException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public ApiException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
