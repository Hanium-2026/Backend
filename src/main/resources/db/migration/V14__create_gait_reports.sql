CREATE TABLE gait_reports
(
    report_id         BIGSERIAL   PRIMARY KEY,
    ward_id           BIGINT      NOT NULL REFERENCES wards (ward_id),
    session_id        BIGINT      NOT NULL UNIQUE REFERENCES gait_sessions (session_id),
    risk_level        VARCHAR(20) NOT NULL,
    avg_score         FLOAT,
    min_score         FLOAT,
    max_score         FLOAT,
    danger_count      INT         NOT NULL DEFAULT 0,
    variability_score FLOAT,
    asymmetry_score   FLOAT,
    report_summary    TEXT,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
