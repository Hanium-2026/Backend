package com.nevo.nevo.session.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.report.entity.GaitReport;
import com.nevo.nevo.report.entity.RiskLevel;
import com.nevo.nevo.report.repository.GaitReportRepository;
import com.nevo.nevo.session.dto.request.SessionRequest;
import com.nevo.nevo.session.dto.response.SessionResponse;
import com.nevo.nevo.session.entity.GaitSession;
import com.nevo.nevo.session.entity.SessionStatus;
import com.nevo.nevo.session.event.StrokeDangerEvent;
import com.nevo.nevo.session.exception.code.SessionErrorCode;
import com.nevo.nevo.session.exception.code.SessionSuccessCode;
import com.nevo.nevo.session.mapper.SessionResponseMapper;
import com.nevo.nevo.session.repository.GaitSessionRepository;
import com.nevo.nevo.session.repository.SessionScoreRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SessionService {

    private final GaitSessionRepository gaitSessionRepository;
    private final SessionScoreRepository sessionScoreRepository;
    private final GaitReportRepository gaitReportRepository;
    private final WardRepository wardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    // 보행 세션 시작
    // 하루당 1개 세션 보장: ACTIVE 세션이 이미 존재하면 SESSION409 반환
    @Transactional
    public SessionResponse.Start start(Long wardId) {

        // WARD인지 확인
        if (wardId == null) {
            log.warn("[보행 세션 시작] WARD만이 세션을 시작 할 수 있습니다.");
            throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        }

        log.info("[보행 세션 시작] 세션 시작 wardId = {}", wardId);

        // 이미 진행중인(ACTIVE) 세션이 있는지 확인
        if (gaitSessionRepository.existsByWard_IdAndStatus(wardId, SessionStatus.ACTIVE)) {
            log.warn("[보행 세션 시작] 이미 진행중인 세션이 있습니다.");
            throw new CustomException(SessionErrorCode.SESSION_ALREADY_ACTIVE);
        }

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> {
                    log.warn("[보행 세션 시작] WARD 정보를 찾을 수 없습니다.");
                    return new CustomException(WardErrorCode.WARD_NOT_FOUND);
                });

        // 세션 생성 및 저장
        GaitSession session = GaitSession.create(ward);

        gaitSessionRepository.save(session);

        log.info("[보행 세션 시작] 세션 시작 완료 wardId = {}", wardId);

        return SessionResponseMapper
                .toStartResponse(session.getId(), session.getStartedAt());
    }

    // 현재 진행 중인 세션 조회
    // 앱 재시작(배터리 방전 등) 시 로컬 sessionId 복원용
    public ResponseEntity<SuccessResponse<SessionResponse.Active>> getActive(Long wardId) {
        GaitSession session = gaitSessionRepository.findByWard_IdAndStatus(wardId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(SessionErrorCode.SESSION_NOT_FOUND));

        return ResponseEntity.ok(SuccessResponse.of(
                SessionSuccessCode.SESSION_ACTIVE_FOUND,
                SessionResponse.Active.builder()
                        .sessionId(session.getId())
                        .startedAt(session.getStartedAt())
                        .build()
        ));
    }

    // 보행 세션 수동 종료
    // COMPLETED면 멱등 처리(200 반환)
    @Transactional
    public ResponseEntity<SuccessResponse<Void>> stop(Long sessionId, Long wardId) {
        GaitSession session = findSessionAndValidateOwner(sessionId, wardId);

        // 이미 종료된 세션이면 멱등 처리 (앱 재시도 대비)
        if (session.getStatus() == SessionStatus.COMPLETED) {
            return ResponseEntity.ok(SuccessResponse.of(SessionSuccessCode.SESSION_STOPPED));
        }

        session.complete(LocalDateTime.now());

        return ResponseEntity.ok(SuccessResponse.of(SessionSuccessCode.SESSION_STOPPED));
    }

    // 분당 보행 데이터 배치 업로드
    // 앱이 SQLite에 쌓아둔 데이터를 네트워크 복구 시 한 번에 전송하는 오프라인 우선 설계
    @Transactional
    public ResponseEntity<SuccessResponse<SessionResponse.DataUpload>> uploadData(Long sessionId, Long wardId,
                                                                                   SessionRequest.DataUpload request) {
        GaitSession session = findSessionAndValidateOwner(sessionId, wardId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = session.getStartedAt().truncatedTo(ChronoUnit.MINUTES);

        // 미래 시각 또는 세션 시작 이전 데이터 제외 (앱 시계 오류 방어)
        List<SessionRequest.MinuteData> valid = request.data().stream()
                .filter(d -> {
                    LocalDateTime t = d.minuteAt().truncatedTo(ChronoUnit.MINUTES);
                    return !t.isAfter(now) && !t.isBefore(sessionStart);
                })
                .toList();

        int outOfRange = request.data().size() - valid.size();

        if (valid.isEmpty()) {
            return ResponseEntity.ok(SuccessResponse.of(
                    SessionSuccessCode.SESSION_DATA_UPLOADED,
                    SessionResponse.DataUpload.builder().saved(0).skipped(outOfRange).build()
            ));
        }

        // ON CONFLICT DO NOTHING: 네트워크 재시도로 인한 중복 전송 자동 무시
        int[][] results = jdbcTemplate.batchUpdate(
                "INSERT INTO session_scores (session_id, minute_at, avg_score, min_score, max_score, danger_count, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (session_id, minute_at) DO NOTHING",
                valid,
                valid.size(),
                (ps, d) -> {
                    // minuteAt을 분 단위로 잘라 UNIQUE(session_id, minute_at) 제약 활용
                    LocalDateTime minuteAt = d.minuteAt().truncatedTo(ChronoUnit.MINUTES);
                    ps.setLong(1, sessionId);
                    ps.setTimestamp(2, Timestamp.valueOf(minuteAt));
                    ps.setFloat(3, d.avgScore());
                    ps.setFloat(4, d.minScore());
                    ps.setFloat(5, d.maxScore());
                    ps.setInt(6, d.dangerCount());
                    ps.setTimestamp(7, Timestamp.valueOf(now));
                }
        );

        // ParameterizedPreparedStatementSetter 버전은 int[][]를 반환: 배치 그룹별 결과 배열
        int saved = (int) Arrays.stream(results).flatMapToInt(Arrays::stream).filter(r -> r == 1).count();
        int skipped = outOfRange + (valid.size() - saved);

        return ResponseEntity.ok(SuccessResponse.of(
                SessionSuccessCode.SESSION_DATA_UPLOADED,
                SessionResponse.DataUpload.builder()
                        .saved(saved)
                        .skipped(skipped)
                        .build()
        ));
    }

    // 세션 종합 분석 결과 업로드
    // 앱이 세션 종료 후 TFLite 분석을 완료하면 호출, gait_reports에 저장하고 daily_scores 갱신
    @Transactional
    public ResponseEntity<SuccessResponse<Void>> uploadAnalysis(Long sessionId, Long wardId,
                                                                 SessionRequest.AnalysisUpload request) {
        GaitSession session = findSessionAndValidateOwner(sessionId, wardId);

        // 중복 분석 업로드 → 멱등 처리 (네트워크 재시도 대비)
        if (gaitReportRepository.findBySession_Id(sessionId).isPresent()) {
            return ResponseEntity.ok(SuccessResponse.of(SessionSuccessCode.ANALYSIS_UPLOADED));
        }

        Ward ward = session.getWard();

        LocalDateTime now = LocalDateTime.now();

        gaitReportRepository.save(
                GaitReport.builder()
                        .ward(ward)
                        .session(session)
                        .riskLevel(RiskLevel.valueOf(request.riskLevel()))
                        .avgScore(request.avgScore())
                        .minScore(request.minScore())
                        .maxScore(request.maxScore())
                        .dangerCount(request.dangerCount())
                        .reportSummary(request.reportSummary())
                        .variabilityScore(request.variabilityScore())
                        .asymmetryScore(request.asymmetryScore())
                        .createdAt(now)
                        .build()
        );

        if (request.dangerCount() > 0) {
            // 위험 세션: expires_at = NULL 유지(영구 보관) + FCM 알림 이벤트 발행
            session.markStrokeDanger();
            eventPublisher.publishEvent(new StrokeDangerEvent(wardId, sessionId));
        } else {
            // 정상 세션: session_scores 7일 후 자동 삭제 예약
            sessionScoreRepository.updateExpiresAtBySessionId(sessionId, now.plusDays(7));
        }

        upsertDailyScore(wardId, request.avgScore(), request.minScore(), request.maxScore(),
                request.variabilityScore(), request.asymmetryScore(), request.dangerCount());

        return ResponseEntity.ok(SuccessResponse.of(SessionSuccessCode.ANALYSIS_UPLOADED));
    }

    private GaitSession findSessionAndValidateOwner(Long sessionId, Long wardId) {
        GaitSession session = gaitSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(SessionErrorCode.SESSION_NOT_FOUND));
        if (!session.getWard().getId().equals(wardId)) {
            throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        }
        return session;
    }

    // 일별 보행 통계 원자적 UPSERT
    // 당일 첫 세션이면 INSERT, 이후 세션이면 누적 평균·최솟값·최댓값 갱신
    // ON CONFLICT DO UPDATE로 SELECT 후 UPDATE 패턴의 레이스 컨디션 방지
    private void upsertDailyScore(Long wardId, Float avgScore, Float minScore, Float maxScore,
                                   Float variabilityScore, Float asymmetryScore, Integer dangerCount) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO daily_scores (ward_id, date, avg_score, min_score, max_score,
                                          variability_score, asymmetry_score, danger_count,
                                          session_count, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                ON CONFLICT (ward_id, date) DO UPDATE SET
                  avg_score         = (daily_scores.avg_score * daily_scores.session_count + EXCLUDED.avg_score)
                                      / (daily_scores.session_count + 1),
                  min_score         = LEAST(daily_scores.min_score, EXCLUDED.min_score),
                  max_score         = GREATEST(daily_scores.max_score, EXCLUDED.max_score),
                  variability_score = CASE WHEN EXCLUDED.variability_score IS NULL THEN daily_scores.variability_score
                                          ELSE (COALESCE(daily_scores.variability_score, 0) * daily_scores.session_count + EXCLUDED.variability_score)
                                               / (daily_scores.session_count + 1) END,
                  asymmetry_score   = CASE WHEN EXCLUDED.asymmetry_score IS NULL THEN daily_scores.asymmetry_score
                                          ELSE (COALESCE(daily_scores.asymmetry_score, 0) * daily_scores.session_count + EXCLUDED.asymmetry_score)
                                               / (daily_scores.session_count + 1) END,
                  danger_count      = daily_scores.danger_count + EXCLUDED.danger_count,
                  session_count     = daily_scores.session_count + 1,
                  updated_at        = EXCLUDED.updated_at
                """,
                wardId, Date.valueOf(now.toLocalDate()), avgScore, minScore, maxScore,
                variabilityScore, asymmetryScore, dangerCount,
                Timestamp.valueOf(now), Timestamp.valueOf(now)
        );
    }
}
