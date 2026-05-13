CREATE TABLE wards (
    ward_id BIGSERIAL PRIMARY KEY,
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    birth_date DATE,
    gender VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);