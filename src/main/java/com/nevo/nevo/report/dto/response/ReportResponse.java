package com.nevo.nevo.report.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportResponse {

    // GET /api/gait/reports/{sessionId}
    // 특정 세션의 보행 분석 결과 상세 (WARD: 본인 세션, GUARDIAN: 연동된 노약자 세션)
    @Builder
    public record SessionDetail(
            Long sessionId,
            LocalDateTime createdAt,
            String riskLevel,
            Float avgScore,
            Float minScore,
            Float maxScore,
            Integer dangerCount,
            Float variabilityScore,
            Float symmetryScore,      // asymmetryScore 변환값: (1 - asymmetryScore) * 100
            String reportSummary
    ) {}

    // GET /api/gait/reports/daily, GET /api/gait/reports/ward/{wardId}/daily
    // daily_scores 테이블의 1일치 집계 행. 하루에 세션이 여러 개면 가중 평균으로 누적됨
    @Builder
    public record DailyStats(
            LocalDate date,
            Float avgScore,
            Float minScore,
            Float maxScore,
            Integer sessionCount,
            Integer dangerCount,
            Float variabilityScore,
            Float symmetryScore       // asymmetryScore 변환값: (1 - asymmetryScore) * 100
    ) {}

    // GET /api/gait/reports/daily
    // WARD 히스토리 화면 세션 목록용. gait_reports 기반 세션별 요약
    @Builder
    public record SessionSummary(
            Long sessionId,
            LocalDateTime createdAt,
            String riskLevel,
            Float avgScore,
            Float symmetryScore       // asymmetryScore 변환값: (1 - asymmetryScore) * 100
    ) {}

    // GET /api/gait/reports/daily
    // WARD 전용. 최근 7일 일별 통계 + 세션 목록
    @Builder
    public record DailyReport(
            List<DailyStats> dailyScores,
            List<SessionSummary> sessions
    ) {}

    // GET /api/gait/reports/ward/{wardId}/daily
    // 오늘 날짜의 daily_scores 행. 오늘 측정 기록이 없으면 null
    @Builder
    public record TodayMetrics(
            Float avgScore,
            Float minScore,
            Float maxScore,
            Float variabilityScore,
            Float symmetryScore,      // asymmetryScore 변환값: (1 - asymmetryScore) * 100
            Integer sessionCount,
            Integer dangerCount
    ) {}

    // GET /api/gait/reports/ward/{wardId}/daily
    // GUARDIAN 전용. 기간별(days=7/30/90) 일별 통계 + 오늘 핵심 지표
    @Builder
    public record GuardianDailyReport(
            // 7/30/90일치 날짜별 집계 (하루 단위)
            List<DailyStats> dailyScores,
            // 오늘 하루 집계값 (평균 점수, 위험 횟수 등)
            TodayMetrics todayMetrics,

            List<SessionSummary> sessions
    ) {}

    // GET /api/gait/reports/dashboard
    // 노약자 1명의 요약 카드. latestScore/riskLevel/lastSessionAt은 gait_reports 최신 1건, 없으면 null
    @Builder
    public record WardSummary(
            Long wardId,
            String name,
            Float latestScore,
            String riskLevel,
            LocalDateTime lastSessionAt,  // 최근 측정 시각 (gait_reports.created_at)
            List<Float> trend   // 최근 7일 avgScore 배열 (항상 7개, 없는 날 null)
    ) {}

    // GET /api/gait/reports/dashboard
    // GUARDIAN 전용. 연동된 모든 노약자의 최신 보행 상태 요약
    @Builder
    public record Dashboard(List<WardSummary> wards) {}
}
