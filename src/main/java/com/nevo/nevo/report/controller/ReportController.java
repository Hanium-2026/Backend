package com.nevo.nevo.report.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.report.dto.response.ReportResponse;
import com.nevo.nevo.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/gait/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "보행 리포트 API")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{sessionId}")
    @Operation(summary = "세션 리포트 조회", description = "특정 세션의 보행 분석 결과를 조회합니다. (WARD: 본인 세션, GUARDIAN: 연동된 노약자 세션)")
    public ResponseEntity<SuccessResponse<ReportResponse.SessionDetail>> getSessionReport(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long sessionId
    ) {
        return reportService.getSessionReport(sessionId, auth);
    }

    @GetMapping("/daily")
    @Operation(summary = "최근 7일 보행 통계 조회", description = "최근 7일간의 일별 점수 추이와 세션 목록을 조회합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<ReportResponse.DailyReport>> getDailyReport(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        return reportService.getDailyReport(auth);
    }

    @GetMapping("/ward/{wardId}/daily")
    @Operation(summary = "보호자용 노약자 보행 통계 조회", description = "연동된 노약자의 기간별 보행 통계. days=7/30/90 지원. (GUARDIAN 전용)")
    public ResponseEntity<SuccessResponse<ReportResponse.GuardianDailyReport>> getGuardianDailyReport(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long wardId,
            @RequestParam(defaultValue = "7") @Min(7) @Max(90) int days
    ) {
        return reportService.getGuardianDailyReport(wardId, days, auth);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "보호자 대시보드 조회", description = "연동된 모든 노약자의 최신 보행 상태 요약. (GUARDIAN 전용)")
    public ResponseEntity<SuccessResponse<ReportResponse.Dashboard>> getDashboard(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        return reportService.getDashboard(auth);
    }
}
