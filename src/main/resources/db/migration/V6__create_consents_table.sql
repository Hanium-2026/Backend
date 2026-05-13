CREATE TABLE consents (
    id           BIGSERIAL   PRIMARY KEY,
    consent_type VARCHAR(50) NOT NULL,
    agreed       BOOLEAN     NOT NULL,
    expire_at    TIMESTAMP,
    agreed_at    TIMESTAMP,
    user_id      BIGINT      NOT NULL REFERENCES users(user_id) ON DELETE CASCADE
);
