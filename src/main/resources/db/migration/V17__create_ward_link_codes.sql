    CREATE TABLE ward_link_codes (
    code_id    BIGSERIAL   PRIMARY KEY,
    ward_id    BIGINT      NOT NULL,
    code       VARCHAR(8)  NOT NULL UNIQUE,
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wlc_ward
        FOREIGN KEY (ward_id) REFERENCES wards (ward_id) ON DELETE CASCADE
);

CREATE INDEX idx_wlc_code ON ward_link_codes (code);
CREATE INDEX idx_wlc_ward_id ON ward_link_codes (ward_id);
CREATE INDEX idx_wlc_expires_at ON ward_link_codes (expires_at);