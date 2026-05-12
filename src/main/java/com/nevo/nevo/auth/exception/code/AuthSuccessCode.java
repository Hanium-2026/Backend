package com.nevo.nevo.auth.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

    LOGIN_SUCCESS(HttpStatus.OK, "AUTH200", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH2001", "로그아웃에 성공했습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AUTH2002", "토큰 갱신에 성공했습니다."),
    PASSWORD_RESET_REQUEST_SUCCESS(HttpStatus.OK, "AUTH2003", "비밀번호 재설정 이메일을 발송했습니다."),
    PASSWORD_RESET_SUCCESS(HttpStatus.OK, "AUTH2004", "비밀번호가 성공적으로 변경되었습니다."),
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "AUTH201", "회원가입에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
