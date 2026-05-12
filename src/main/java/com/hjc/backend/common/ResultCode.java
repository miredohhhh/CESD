package com.hjc.backend.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    VALIDATION_ERROR(422, "Validation error"),
    INTERNAL_ERROR(500, "Internal server error"),
    BUSINESS_ERROR(1000, "Business error");

    private final Integer code;

    private final String message;
}
