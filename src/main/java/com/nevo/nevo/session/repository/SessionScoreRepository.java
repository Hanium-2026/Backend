package com.nevo.nevo.session.repository;

import com.nevo.nevo.session.entity.SessionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SessionScoreRepository extends JpaRepository<SessionScore, Long> {

    @Modifying
    @Query("UPDATE SessionScore s SET s.expiresAt = :expiresAt WHERE s.session.id = :sessionId")
    void updateExpiresAtBySessionId(@Param("sessionId") Long sessionId, @Param("expiresAt") LocalDateTime expiresAt);

    @Modifying
    @Query("DELETE FROM SessionScore s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    int deleteExpiredScores(@Param("now") LocalDateTime now);

    /**
     * analysis 없이 종료된 세션의 session_scores 만료 처리 안전망.
     * 앱 크래시/네트워크 오류로 analysis가 전송되지 않은 경우,
     * expires_at=NULL(영구 보관)인 정상 세션 데이터가 무기한 누적되는 것을 방지.
     *
     * 조건: COMPLETED 상태이며 ended_at이 하루 이상 지났고 gait_reports가 없는 세션
     * 처리: expires_at = now + 7일 (정상 세션 보관 정책 적용)
     */
    @Modifying
    @Query(value = """
            UPDATE session_scores
            SET expires_at = :expiresAt
            WHERE expires_at IS NULL
            AND session_id IN (
                SELECT s.session_id FROM gait_sessions s
                LEFT JOIN gait_reports r ON s.session_id = r.session_id
                WHERE s.status = 'COMPLETED'
                AND s.ended_at < :threshold
                AND r.report_id IS NULL
            )
            """, nativeQuery = true)
    int expireOrphanedScores(@Param("expiresAt") LocalDateTime expiresAt,
                             @Param("threshold") LocalDateTime threshold);

}
