CREATE TABLE ward_guardian_link (
    link_id          BIGSERIAL PRIMARY KEY,
    ward_id          BIGINT    NOT NULL,
    guardian_user_id BIGINT    NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,

    CONSTRAINT fk_wgl_ward
        FOREIGN KEY (ward_id) REFERENCES wards (ward_id) ON DELETE CASCADE,
    CONSTRAINT fk_wgl_guardian
        FOREIGN KEY (guardian_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT uq_wgl_ward_guardian
        UNIQUE (ward_id, guardian_user_id)
);

CREATE INDEX idx_wgl_ward_id ON ward_guardian_link (ward_id);
CREATE INDEX idx_wgl_guardian_user_id ON ward_guardian_link (guardian_user_id);