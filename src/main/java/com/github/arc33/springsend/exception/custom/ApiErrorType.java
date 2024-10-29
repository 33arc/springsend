package com.github.arc33.springsend.exception.custom;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ApiErrorType {
    // Authentication & Authorization
    NOT_AUTHORIZED(ApiErrorCategory.API_ERROR, "not_authorized", "User is not authorized to perform this action", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(ApiErrorCategory.API_ERROR, "invalid_credentials", "The provided credentials are invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(ApiErrorCategory.API_ERROR, "token_expired", "The authentication token has expired", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_PERMISSIONS(ApiErrorCategory.API_ERROR, "insufficient_permissions", "User lacks required permissions", HttpStatus.FORBIDDEN),

    // Resource Related
    RESOURCE_NOT_FOUND(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_not_found", "The requested resource was not found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_already_exists", "Resource already exists", HttpStatus.CONFLICT),
    RESOURCE_DELETED(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_deleted", "Resource has been deleted", HttpStatus.GONE),

    // Input Validation
    INVALID_FIELD_TYPE(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_field_type", "The type of the field is invalid", HttpStatus.BAD_REQUEST),
    INVALID_FIELD_VALUE(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_field_value", "The value of the field is invalid", HttpStatus.BAD_REQUEST),
    MISSING_FIELD(ApiErrorCategory.INVALID_REQUEST_ERROR, "missing_field", "Required field is missing from the request", HttpStatus.BAD_REQUEST),
    UNKNOWN_FIELD(ApiErrorCategory.INVALID_REQUEST_ERROR, "unknown_field", "Unknown field provided in request", HttpStatus.BAD_REQUEST),

    // Format Validation
    INVALID_FORMAT(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_format", "The format of the request is invalid", HttpStatus.BAD_REQUEST),
    INVALID_JSON(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_json", "Request payload is not valid JSON", HttpStatus.BAD_REQUEST),
    INVALID_ARRAY(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_array", "Array structure in request is invalid", HttpStatus.BAD_REQUEST),

    // Parameter Validation
    INVALID_PARAMETER(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_parameter", "One or more request parameters are invalid", HttpStatus.BAD_REQUEST),
    INVALID_QUERY_PARAM(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_query_param", "Query parameter is invalid", HttpStatus.BAD_REQUEST),
    INVALID_PATH_PARAM(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_path_param", "Path parameter is invalid", HttpStatus.BAD_REQUEST),

    // Resource Related
    RESOURCE_MISSING(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_missing", "The requested resource does not exist", HttpStatus.NOT_FOUND),
    RESOURCE_CONFLICT(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_conflict", "Resource conflict in request", HttpStatus.CONFLICT),
    RESOURCE_ARCHIVED(ApiErrorCategory.INVALID_REQUEST_ERROR, "resource_archived", "The requested resource is archived", HttpStatus.GONE),

    // Request Structure
    MALFORMED_REQUEST(ApiErrorCategory.INVALID_REQUEST_ERROR, "malformed_request", "The request structure is malformed", HttpStatus.BAD_REQUEST),
    INVALID_CONTENT_TYPE(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_content_type", "Invalid content type in request", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    UNSUPPORTED_MEDIA_TYPE(ApiErrorCategory.INVALID_REQUEST_ERROR, "unsupported_media_type", "Media type is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // Authentication/Authorization Related
    INVALID_API_KEY(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_api_key", "The provided API key is invalid", HttpStatus.UNAUTHORIZED),
    INVALID_AUTH_HEADER(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_auth_header", "Invalid authorization header", HttpStatus.UNAUTHORIZED),
    MISSING_AUTH_TOKEN(ApiErrorCategory.INVALID_REQUEST_ERROR, "missing_auth_token", "Authentication token is missing", HttpStatus.UNAUTHORIZED),

    // Business Validation
    INVALID_STATE(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_state", "Invalid state for requested operation", HttpStatus.CONFLICT),
    INVALID_OPERATION(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_operation", "The requested operation is invalid", HttpStatus.BAD_REQUEST),
    INVALID_TRANSITION(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_transition", "Invalid state transition requested", HttpStatus.CONFLICT),

    // Limits and Constraints
    EXCEEDED_MAX_LENGTH(ApiErrorCategory.INVALID_REQUEST_ERROR, "exceeded_max_length", "Field exceeds maximum length", HttpStatus.BAD_REQUEST),
    BELOW_MIN_LENGTH(ApiErrorCategory.INVALID_REQUEST_ERROR, "below_min_length", "Field below minimum length", HttpStatus.BAD_REQUEST),
    EXCEEDED_SIZE_LIMIT(ApiErrorCategory.INVALID_REQUEST_ERROR, "exceeded_size_limit", "Request size exceeds limit", HttpStatus.PAYLOAD_TOO_LARGE),

    // Metadata Related
    INVALID_METADATA(ApiErrorCategory.INVALID_REQUEST_ERROR, "invalid_metadata", "Invalid metadata in request", HttpStatus.BAD_REQUEST),
    METADATA_PARSE_ERROR(ApiErrorCategory.INVALID_REQUEST_ERROR, "metadata_parse_error", "Unable to parse metadata", HttpStatus.BAD_REQUEST),

    // Version Related
    UNSUPPORTED_API_VERSION(ApiErrorCategory.INVALID_REQUEST_ERROR, "unsupported_api_version", "API version is not supported", HttpStatus.BAD_REQUEST),
    DEPRECATED_FIELD_USAGE(ApiErrorCategory.INVALID_REQUEST_ERROR, "deprecated_field_usage", "Usage of deprecated field detected", HttpStatus.BAD_REQUEST),

    // Business Logic
    BUSINESS_RULE_VIOLATION(ApiErrorCategory.API_ERROR, "business_rule_violation", "Business rule violation occurred", HttpStatus.UNPROCESSABLE_ENTITY),
    OPERATION_NOT_ALLOWED(ApiErrorCategory.API_ERROR, "operation_not_allowed", "Operation is not allowed", HttpStatus.METHOD_NOT_ALLOWED),

    // System/Technical
    INTERNAL_ERROR(ApiErrorCategory.API_ERROR, "internal_error", "An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(ApiErrorCategory.API_ERROR, "service_unavailable", "Service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR(ApiErrorCategory.API_ERROR, "database_error", "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NETWORK_ERROR(ApiErrorCategory.API_ERROR, "network_error", "Network communication error", HttpStatus.BAD_GATEWAY),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(ApiErrorCategory.API_ERROR, "rate_limit_exceeded", "Rate limit has been exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // Data State
    STALE_DATA(ApiErrorCategory.API_ERROR, "stale_data", "Data is outdated and needs refresh", HttpStatus.CONFLICT),
    CONFLICT(ApiErrorCategory.API_ERROR, "conflict", "Data conflict detected", HttpStatus.CONFLICT),

    // Integration
    EXTERNAL_SERVICE_ERROR(ApiErrorCategory.API_ERROR, "external_service_error", "External service integration failed", HttpStatus.BAD_GATEWAY),
    API_VERSION_DEPRECATED(ApiErrorCategory.API_ERROR, "api_version_deprecated", "API version is deprecated", HttpStatus.GONE),

    // Payment Processing
    CARD_PROCESSING_ERROR(ApiErrorCategory.CARD_ERROR, "card_processing_error", "Error processing the card", HttpStatus.UNPROCESSABLE_ENTITY),
    CARD_DECLINED(ApiErrorCategory.CARD_ERROR, "card_declined", "The card was declined", HttpStatus.PAYMENT_REQUIRED),
    CARD_EXPIRED(ApiErrorCategory.CARD_ERROR, "card_expired", "The card has expired", HttpStatus.PAYMENT_REQUIRED),

    // Idempotency
    DUPLICATE_REQUEST(ApiErrorCategory.IDEMPOTENCY_ERROR, "duplicate_request", "Duplicate request detected", HttpStatus.CONFLICT),
    INVALID_IDEMPOTENCY_KEY(ApiErrorCategory.IDEMPOTENCY_ERROR, "invalid_idempotency_key", "Invalid idempotency key", HttpStatus.BAD_REQUEST);

    private final ApiErrorCategory category;
    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    public String getType() {
        return category.getValue();
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorResponse toErrorResponse() {
        return ErrorResponse.builder()
                .code(this.code)
                .type(this.category.getValue())
                .description(this.defaultMessage)
                .status(this.httpStatus)
                .build();
    }

    public ErrorResponse toErrorResponse(String customMessage) {
        return ErrorResponse.builder()
                .code(this.code)
                .type(this.category.getValue())
                .description(customMessage)
                .status(this.httpStatus)
                .build();
    }

    public static ApiErrorType fromCode(String code) {
        for (ApiErrorType errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }

    public ApiException toException() {
        return new ApiException(toErrorResponse());
    }

    public ApiException toException(String customMessage) {
        return new ApiException(toErrorResponse(customMessage));
    }
}