package com.nevo.nevo.global.exception;

import com.nevo.nevo.global.exception.code.ErrorCode;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        String timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                Instant.now().toString()
        );
    }
}
