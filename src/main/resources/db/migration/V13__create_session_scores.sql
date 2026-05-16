CREATE TABLE session_scores
(
    id           BIGSERIAL PRIMARY KEY,
    session_id   BIGINT    NOT NULL REFERENCES gait_sessions (session_id),
    minute_at    TIMESTAMP NOT NULL,
    avg_score    FLOAT     NOT NULL,
    min_score    FLOAT     NOT NULL,
    max_score    FLOAT     NOT NULL,
    danger_count INT       NOT NULL DEFAULT 0,
    expires_at   TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (session_id, minute_at)
);
