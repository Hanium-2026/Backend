CREATE TABLE password_reset_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE
);
