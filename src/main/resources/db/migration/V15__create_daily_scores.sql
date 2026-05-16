CREATE TABLE daily_scores
(
    id            BIGSERIAL PRIMARY KEY,
    ward_id       BIGINT   NOT NULL REFERENCES wards (ward_id),
    date          DATE     NOT NULL,
    avg_score     FLOAT,
    min_score     FLOAT,
    max_score     FLOAT,
    session_count INT      NOT NULL DEFAULT 1,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (ward_id, date)
);
