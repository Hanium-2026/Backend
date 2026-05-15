CREATE TABLE locations (
    location_id BIGSERIAL PRIMARY KEY,
    ward_id     BIGINT          NOT NULL,
    latitude    DECIMAL(10, 8)  NOT NULL,
    longitude   DECIMAL(11, 8)  NOT NULL,
    recorded_at TIMESTAMP       NOT NULL,

    CONSTRAINT fk_locations_ward
        FOREIGN KEY (ward_id)
        REFERENCES wards (ward_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_locations_ward_id ON locations (ward_id);
CREATE INDEX idx_locations_recorded_at ON locations (ward_id, recorded_at DESC);