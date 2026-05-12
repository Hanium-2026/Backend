package com.nevo.nevo.global.exception;

import com.nevo.nevo.global.exception.code.SuccessCode;

import java.time.Instant;

public record SuccessResponse<T>(
        String code,
        String message,
        String timestamp,
        T data
) {
    public static <T> SuccessResponse<T> of(SuccessCode successCode, T data) {
        return new SuccessResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                Instant.now().toString(),
                data
        );
    }

    public static SuccessResponse<Void> of(SuccessCode successCode) {
        return new SuccessResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                Instant.now().toString(),
                null
        );
    }
}
