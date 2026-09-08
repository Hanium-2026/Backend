package com.nevo.nevo.auth.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    WARD_FIELDS_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH4001", "WARD 역할은 키, 몸무게, 생년월일, 성별이 필수입니다."),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH4002", "인증번호가 만료되었습니다."),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "AUTH4003", "인증번호가 올바르지 않습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH4004", "휴대폰 인증이 완료되지 않았습니다."),
    REQUIRED_CONSENT_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH4005", "필수 약관(이용약관, 개인정보처리방침)에 동의해야 합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4011", "전화번호 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "만료된 토큰입니다."),
    REVOKED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4014", "이미 무효화된 토큰입니다."),
    PHONE_DUPLICATED(HttpStatus.CONFLICT, "AUTH409", "이미 사용 중인 전화번호입니다."),
    OTP_MAX_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH4291", "인증 시도 횟수를 초과했습니다. 15분 후 다시 시도해주세요."),
    WARD_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH500", "WARD 데이터를 찾을 수 없습니다. 관리자에게 문의하세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
