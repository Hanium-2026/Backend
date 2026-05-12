package com.nevo.nevo.user.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    USER_FOUND(HttpStatus.OK, "USER200", "사용자 정보를 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
