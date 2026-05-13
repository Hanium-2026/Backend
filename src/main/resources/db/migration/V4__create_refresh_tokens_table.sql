CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    device_id  VARCHAR(255) NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    user_id    BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE
);
