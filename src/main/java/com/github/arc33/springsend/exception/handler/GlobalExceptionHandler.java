package com.github.arc33.springsend.exception.handler;

import com.github.arc33.springsend.exception.custom.ApiException;
import com.github.arc33.springsend.exception.custom.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getErrorResponse().getStatus())
                .body(ex.getErrorResponse());
    }
    
}