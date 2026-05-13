package com.nevo.nevo.ward.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WardSuccessCode implements SuccessCode {

    WARD_PHYSICAL_INFO_FOUND(HttpStatus.OK, "WARD2000", "노약자 신체 정보를 조회했습니다."),
    WARD_PHYSICAL_INFO_UPDATED(HttpStatus.OK, "WARD2001", "노약자 신체 정보를 등록 또는 수정했습니다."),
    WARD_LOCATION_UPDATED(HttpStatus.OK, "WARD2002", "노약자 위치 정보를 전송했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
