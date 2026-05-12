package com.nevo.nevo.user.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "피보호자 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
