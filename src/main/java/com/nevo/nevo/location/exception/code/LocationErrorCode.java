package com.nevo.nevo.location.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocationErrorCode implements ErrorCode {

    GUARDIAN_ONLY(HttpStatus.FORBIDDEN, "LOC4031", "보호자만 접근 가능한 기능입니다."),
    WARD_ONLY(HttpStatus.FORBIDDEN,     "LOC4032", "노약자만 접근 가능한 기능입니다."),
    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "LOC404",  "노약자 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}