package com.nevo.nevo.report.exception.code;

import com.nevo.nevo.global.exception.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportSuccessCode implements SuccessCode {

    REPORT_FOUND(HttpStatus.OK, "REPORT200", "리포트를 조회했습니다."),
    WEEKLY_STATS_FOUND(HttpStatus.OK, "REPORT2001", "주간 보행 통계를 조회했습니다."),
    GUARDIAN_DAILY_FOUND(HttpStatus.OK, "REPORT2002", "보호자용 보행 통계를 조회했습니다."),
    DASHBOARD_FOUND(HttpStatus.OK, "REPORT2003", "보호자 대시보드를 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
