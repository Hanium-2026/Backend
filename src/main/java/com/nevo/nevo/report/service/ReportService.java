package com.nevo.nevo.report.service;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.report.dto.response.ReportResponse;
import com.nevo.nevo.report.entity.DailyScore;
import com.nevo.nevo.report.entity.GaitReport;
import com.nevo.nevo.report.exception.code.ReportErrorCode;
import com.nevo.nevo.report.exception.code.ReportSuccessCode;
import com.nevo.nevo.report.repository.DailyScoreRepository;
import com.nevo.nevo.report.repository.GaitReportRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.entity.WardGuardianLink;
import com.nevo.nevo.ward.repository.WardGuardianLinkRepository;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final GaitReportRepository gaitReportRepository;
    private final DailyScoreRepository dailyScoreRepository;
    private final WardGuardianLinkRepository wardGuardianLinkRepository;
    private final WardRepository wardRepository;

    // GET /api/gait/reports/{sessionId}
    // WARD: 본인 세션인지 wardId 일치 확인
    // GUARDIAN: ward_guardian_link 연동 여부 확인
    public ResponseEntity<SuccessResponse<ReportResponse.SessionDetail>> getSessionReport(
            Long sessionId, JwtAuthentication auth) {

        GaitReport report = gaitReportRepository.findBySession_Id(sessionId)
                .orElseThrow(() -> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        Long wardId = report.getWard().getId();

        if (auth.wardId() != null) {
            // WARD: 본인 세션인지 확인
            if (!auth.wardId().equals(wardId)) {
                throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
            }
        } else {
            // GUARDIAN: 연동된 노약자인지 확인
            if (!wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, auth.userId())) {
                throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
            }
        }

        Float symmetryScore = toSymmetryScore(report.getAsymmetryScore());

        return ResponseEntity.ok(SuccessResponse.of(
                ReportSuccessCode.REPORT_FOUND,
                ReportResponse.SessionDetail.builder()
                        .sessionId(report.getSession().getId())
                        .createdAt(report.getCreatedAt())
                        .riskLevel(report.getRiskLevel().name())
                        .avgScore(report.getAvgScore())
                        .minScore(report.getMinScore())
                        .maxScore(report.getMaxScore())
                        .dangerCount(report.getDangerCount())
                        .variabilityScore(report.getVariabilityScore())
                        .symmetryScore(symmetryScore)
                        .reportSummary(report.getReportSummary())
                        .build()
        ));
    }

    // GET /api/gait/reports/daily (WARD 전용)
    // daily_scores: 최근 7일 일별 집계 (오늘 포함, 측정 없는 날은 생략)
    // sessions: gait_reports 기반 세션 목록 (히스토리 화면 하단 측정 기록)
    public ResponseEntity<SuccessResponse<ReportResponse.DailyReport>> getDailyReport(JwtAuthentication auth) {
        if (auth.wardId() == null) {
            throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
        }

        Long wardId = auth.wardId();
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);

        List<DailyScore> dailyScores = dailyScoreRepository
                .findByWard_IdAndDateBetweenOrderByDateAsc(wardId, from, today);
    
        List<GaitReport> reports = gaitReportRepository
                .findAllByWard_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        wardId,
                        from.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                );

        List<ReportResponse.DailyStats> dailyStatsList = dailyScores.stream()
                .map(this::toDailyStats)
                .toList();

        List<ReportResponse.SessionSummary> sessionList = reports.stream()
                .map(r -> ReportResponse.SessionSummary.builder()
                        .sessionId(r.getSession().getId())
                        .createdAt(r.getCreatedAt())
                        .riskLevel(r.getRiskLevel().name())
                        .avgScore(r.getAvgScore())
                        .symmetryScore(toSymmetryScore(r.getAsymmetryScore()))
                        .build())
                .toList();

        return ResponseEntity.ok(SuccessResponse.of(
                ReportSuccessCode.WEEKLY_STATS_FOUND,
                ReportResponse.DailyReport.builder()
                        .dailyScores(dailyStatsList)
                        .sessions(sessionList)
                        .build()
        ));
    }

    // GET /api/gait/reports/ward/{wardId}/daily (GUARDIAN 전용)
    // ward_guardian_link 연동 확인 후 노약자의 기간별(days=7/30/90) 일별 통계 반환
    // todayMetrics: 별도 쿼리 없이 dailyScores 결과에서 오늘 항목을 추출해 변환
    public ResponseEntity<SuccessResponse<ReportResponse.GuardianDailyReport>> getGuardianDailyReport(
            Long wardId, int days, JwtAuthentication auth) {

        if (auth.wardId() != null) {
            throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
        }

        if (!wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, auth.userId())) {
            throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
        }

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(days - 1);

        List<DailyScore> dailyScores = dailyScoreRepository
                .findByWard_IdAndDateBetweenOrderByDateAsc(wardId, from, today);

        List<ReportResponse.DailyStats> dailyStatsList = dailyScores.stream()
                .map(this::toDailyStats)
                .toList();

        ReportResponse.TodayMetrics todayMetrics = dailyScores.stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .map(d -> ReportResponse.TodayMetrics.builder()
                        .avgScore(d.getAvgScore())
                        .minScore(d.getMinScore())
                        .maxScore(d.getMaxScore())
                        .variabilityScore(d.getVariabilityScore())
                        .symmetryScore(toSymmetryScore(d.getAsymmetryScore()))
                        .sessionCount(d.getSessionCount())
                        .build())
                .orElse(null);

        return ResponseEntity.ok(SuccessResponse.of(
                ReportSuccessCode.GUARDIAN_DAILY_FOUND,
                ReportResponse.GuardianDailyReport.builder()
                        .dailyScores(dailyStatsList)
                        .todayMetrics(todayMetrics)
                        .build()
        ));
    }

    // GET /api/gait/reports/dashboard (GUARDIAN 전용)
    // 연동된 모든 노약자의 최신 보행 상태를 총 4번 쿼리로 조회
    public ResponseEntity<SuccessResponse<ReportResponse.Dashboard>> getDashboard(JwtAuthentication auth) {
        if (auth.wardId() != null) {
            throw new CustomException(ReportErrorCode.REPORT_FORBIDDEN);
        }

        // 1. 연동된 노약자 목록 조회
        List<WardGuardianLink> links = wardGuardianLinkRepository.findAllByGuardian_Id(auth.userId());
        if (links.isEmpty()) {
            return ResponseEntity.ok(SuccessResponse.of(
                    ReportSuccessCode.DASHBOARD_FOUND,
                    ReportResponse.Dashboard.builder().wards(List.of()).build()
            ));
        }

        List<Long> wardIds = links.stream()
                .map(l -> l.getWard().getId())
                .toList();

        // 2. Ward + User fetch join (1번 쿼리)
        Map<Long, Ward> wardMap = wardRepository.findAllByIdInWithUser(wardIds).stream()
                .collect(Collectors.toMap(Ward::getId, w -> w));

        // 3. 각 ward당 최신 gait_report 1건 (DISTINCT ON, 1번 쿼리)
        Map<Long, GaitReport> latestReportMap = gaitReportRepository.findLatestByWardIdIn(wardIds).stream()
                .collect(Collectors.toMap(r -> r.getWard().getId(), r -> r));

        // 4. 최근 7일 daily_scores 일괄 조회 (IN 쿼리, 1번)
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);
        Map<Long, Map<LocalDate, Float>> trendMap = dailyScoreRepository
                .findByWard_IdInAndDateBetweenOrderByDateAsc(wardIds, from, today)
                .stream()
                .collect(Collectors.groupingBy(
                        d -> d.getWard().getId(),
                        Collectors.toMap(DailyScore::getDate, DailyScore::getAvgScore)
                ));

        // 날짜 슬롯 7개 고정 생성
        List<LocalDate> dateSlots = new ArrayList<>();
        for (int i = 6; i >= 0; i--) dateSlots.add(today.minusDays(i));

        List<ReportResponse.WardSummary> wards = wardIds.stream()
                .map(wardId -> {
                    Ward ward = wardMap.get(wardId);
                    GaitReport report = latestReportMap.get(wardId);
                    Map<LocalDate, Float> scores = trendMap.getOrDefault(wardId, Map.of());

                    List<Float> trend = dateSlots.stream()
                            .map(scores::get)
                            .toList();

                    return ReportResponse.WardSummary.builder()
                            .wardId(wardId)
                            .name(ward != null ? ward.getUser().getName() : null)
                            .latestScore(report != null ? report.getAvgScore() : null)
                            .riskLevel(report != null ? report.getRiskLevel().name() : null)
                            .trend(trend)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(SuccessResponse.of(
                ReportSuccessCode.DASHBOARD_FOUND,
                ReportResponse.Dashboard.builder().wards(wards).build()
        ));
    }

    // daily_scores 행 → DailyStats DTO 변환 (getDailyReport, getGuardianDailyReport 공통 사용)
    private ReportResponse.DailyStats toDailyStats(DailyScore d) {
        return ReportResponse.DailyStats.builder()
                .date(d.getDate())
                .avgScore(d.getAvgScore())
                .minScore(d.getMinScore())
                .maxScore(d.getMaxScore())
                .sessionCount(d.getSessionCount())
                .variabilityScore(d.getVariabilityScore())
                .symmetryScore(toSymmetryScore(d.getAsymmetryScore()))
                .build();
    }

    // asymmetryScore(0~1 raw 임상 지표) → symmetryScore(0~100%) 변환
    // DB에는 비대칭율로 저장, 응답에는 대칭성으로 노출
    private Float toSymmetryScore(Float asymmetryScore) {
        return asymmetryScore != null ? (1 - asymmetryScore) * 100 : null;
    }
}
