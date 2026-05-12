package com.nevo.nevo.auth.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "AUTH400", "유효하지 않거나 만료된 재설정 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4011", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "만료된 토큰입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "AUTH409", "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
