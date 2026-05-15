package com.hjc.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer code;

    private String message;

    private T data;

    private Long timestamp;

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data, System.currentTimeMillis());
    }

    public static ApiResponse<Void> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    public static ApiResponse<Void> fail(ResultCode resultCode, String message) {
        return fail(resultCode.getCode(), message);
    }
}
