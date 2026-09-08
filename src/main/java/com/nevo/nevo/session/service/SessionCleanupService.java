package com.nevo.nevo.session.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private final SessionCleanupStepService stepService;

    // 각 단계를 독립 트랜잭션으로 분리
    // 한 단계가 실패해도 나머지 단계는 독립적으로 커밋 — 특히 세션 자동 종료 실패 시
    // 다음날 사용자가 SESSION_ALREADY_ACTIVE(409)를 겪는 상황 방지
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void cleanUp() {
        stepService.deleteExpiredScores();
        stepService.completeActiveSessions();
        stepService.expireOrphanedScores();

        // 매일 자정에 db에 쌓인 만료된 토큰 제거
        stepService.deleteExpiredRefreshTokens();
    }
}
