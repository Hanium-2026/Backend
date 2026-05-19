CREATE TABLE alerts (
    alert_id   BIGSERIAL    PRIMARY KEY,
    ward_id    BIGINT       NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    session_id BIGINT,
    message    VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_alerts_ward
        FOREIGN KEY (ward_id) REFERENCES wards (ward_id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_ward_id ON alerts (ward_id);
CREATE INDEX idx_alerts_ward_created ON alerts (ward_id, created_at DESC);