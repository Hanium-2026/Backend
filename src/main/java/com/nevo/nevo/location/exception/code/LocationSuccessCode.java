package com.nevo.nevo.location.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocationSuccessCode implements SuccessCode {

    LOCATION_UPDATED(HttpStatus.OK, "LOC200", "위치 정보를 전송했습니다."),
    LOCATION_HISTORY_FOUND(HttpStatus.OK, "LOC2001", "위치 이력을 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}