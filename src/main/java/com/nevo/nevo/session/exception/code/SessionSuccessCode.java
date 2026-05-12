package com.nevo.nevo.session.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SessionSuccessCode implements SuccessCode {

    SESSION_STARTED(HttpStatus.CREATED, "SESSION201", "보행 측정이 시작되었습니다."),
    SESSION_DATA_UPLOADED(HttpStatus.OK, "SESSION200", "보행 데이터가 전송되었습니다."),
    SESSION_STOPPED(HttpStatus.OK, "SESSION2001", "보행 측정이 종료되었습니다."),
    SESSION_ACTIVE_FOUND(HttpStatus.OK, "SESSION2002", "진행 중인 세션을 조회했습니다."),
    ANALYSIS_UPLOADED(HttpStatus.OK, "SESSION2003", "분석 결과가 업로드되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
