-- 세션 API hot path: /start, /active, /stop 모두 ward_id + status 조건으로 조회
CREATE INDEX idx_gait_sessions_ward_status ON gait_sessions (ward_id, status);

-- 자정 스케줄러 만료 삭제 쿼리: expires_at IS NOT NULL AND expires_at < now
-- partial index: expires_at=NULL인 위험 세션(영구 보관)은 제외해 인덱스 크기 최소화
CREATE INDEX idx_session_scores_expires_at ON session_scores (expires_at)
    WHERE expires_at IS NOT NULL;
