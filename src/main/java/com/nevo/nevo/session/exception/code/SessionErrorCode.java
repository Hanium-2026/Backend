package com.nevo.nevo.session.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SessionErrorCode implements ErrorCode {

    SESSION_FORBIDDEN(HttpStatus.FORBIDDEN, "SESSION403", "해당 세션에 접근 권한이 없습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION404", "세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "SESSION409", "이미 진행 중인 세션이 있습니다."),
    SESSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SESSION4091", "이미 종료된 세션입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
