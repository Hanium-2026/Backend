package com.nevo.nevo.session.service;

import com.nevo.nevo.session.entity.SessionStatus;
import com.nevo.nevo.session.repository.GaitSessionRepository;
import com.nevo.nevo.session.repository.SessionScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCleanupStepService {

    private final GaitSessionRepository gaitSessionRepository;
    private final SessionScoreRepository sessionScoreRepository;

    @Transactional
    public void deleteExpiredScores() {
        int deleted = sessionScoreRepository.deleteExpiredScores(LocalDateTime.now());
        log.info("[CleanUp] 만료된 세션 점수 {}건 삭제", deleted);
    }

    @Transactional
    public void completeActiveSessions() {
        // 하루 종일 측정 후 자정에 ACTIVE 세션 자동 종료
        // 사용자가 종료 버튼을 누르지 않아도 다음날 새 세션 시작 가능
        LocalDateTime midnight = LocalDate.now().atStartOfDay();
        var activeSessions = gaitSessionRepository.findByStatus(SessionStatus.ACTIVE);
        activeSessions.forEach(s -> s.complete(midnight));
        log.info("[CleanUp] 세션 {}건 자동 종료 처리", activeSessions.size());
    }

    @Transactional
    public void expireOrphanedScores() {
        // 안전망: analysis 없이 종료된 세션의 session_scores 만료 처리
        // 앱 크래시/네트워크 오류로 analysis가 누락된 경우 데이터 무기한 누적 방지
        // ended_at 기준 하루 이상 지난 세션만 처리 (당일 종료 세션은 앱이 재시도할 수 있으므로 유예)
        int orphaned = sessionScoreRepository.expireOrphanedScores(
                LocalDateTime.now().plusDays(7),  // 정상 세션 보관 정책: 7일 후 삭제
                LocalDateTime.now().minusDays(1)  // 하루 이상 지난 세션만 대상
        );
        log.info("[CleanUp] analysis 누락 세션 점수 {}건 만료 처리", orphaned);
    }
}
