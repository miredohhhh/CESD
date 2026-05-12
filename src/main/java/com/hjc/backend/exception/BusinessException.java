package com.hjc.backend.exception;

import com.hjc.backend.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    private final String message;

    public BusinessException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
