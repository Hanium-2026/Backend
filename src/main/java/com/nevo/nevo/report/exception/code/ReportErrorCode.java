package com.nevo.nevo.report.exception.code;

import com.nevo.nevo.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT404", "리포트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
