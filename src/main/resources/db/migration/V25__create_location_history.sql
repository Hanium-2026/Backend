CREATE TABLE location_history (
    location_history_id BIGSERIAL      PRIMARY KEY,
    ward_id              BIGINT         NOT NULL,
    latitude             DECIMAL(10, 8) NOT NULL,
    longitude            DECIMAL(11, 8) NOT NULL,
    recorded_at          TIMESTAMP      NOT NULL,

    CONSTRAINT fk_location_history_ward
        FOREIGN KEY (ward_id)
        REFERENCES wards (ward_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_location_history_ward_recorded_at ON location_history (ward_id, recorded_at);