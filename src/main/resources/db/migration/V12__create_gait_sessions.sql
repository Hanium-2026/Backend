CREATE TABLE gait_sessions
(
    session_id      BIGSERIAL PRIMARY KEY,
    ward_id         BIGINT      NOT NULL REFERENCES wards (ward_id),
    stroke_detected BOOLEAN     NOT NULL DEFAULT FALSE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at        TIMESTAMP
);
