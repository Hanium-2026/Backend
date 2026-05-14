package com.nevo.nevo.ward.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WardErrorCode implements ErrorCode {

    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "WARD4040", "노약자 정보를 찾을 수 없습니다."),
    WARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WARD403", "노약자만 접근 가능한 기능입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
