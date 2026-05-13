package com.nevo.nevo.user.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    USER_FOUND(HttpStatus.OK, "USER200", "사용자 정보를 조회했습니다."),
    USER_UPDATED(HttpStatus.OK, "USER2001", "사용자 정보를 수정했습니다."),
    FCM_TOKEN_UPDATED(HttpStatus.OK, "USER2002", "기기 토큰을 등록했습니다."),
    USER_DELETED(HttpStatus.OK, "USER2003", "계정이 탈퇴 처리되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
