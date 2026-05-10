-- V1__create_users_table.sql

CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY, -- PostgreSQL에서 자동 증가를 위한 BIGSERIAL
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    fcm_token VARCHAR(255),
    deleted_at TIMESTAMP
);