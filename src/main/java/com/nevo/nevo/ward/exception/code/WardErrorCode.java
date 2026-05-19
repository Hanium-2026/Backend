package com.nevo.nevo.ward.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WardErrorCode implements ErrorCode {

    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "WARD4040", "노약자 정보를 찾을 수 없습니다."),
    WARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WARD403", "노약자만 접근 가능한 기능입니다."),
    GUARDIAN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WARD4031", "보호자만 접근 가능한 기능입니다."),
    WARD_LINK_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "WARD4041", "유효하지 않은 연동 코드입니다."),
    WARD_LINK_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "WARD400", "만료된 연동 코드입니다."),
    WARD_LINK_ALREADY_EXISTS(HttpStatus.CONFLICT, "WARD409", "이미 연결된 노약자입니다."),
    WARD_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "WARD4042", "연결 관계를 찾을 수 없습니다."),
    WARD_LINK_INVALID_GUARDIAN(HttpStatus.BAD_REQUEST, "WARD4001", "보호자 계정이 아닌 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
