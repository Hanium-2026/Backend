ALTER TABLE refresh_tokens
    ADD COLUMN expires_at TIMESTAMP NOT NULL
    DEFAULT NOW() + INTERVAL '30 days';
