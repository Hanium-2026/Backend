ALTER TABLE wards
    ADD COLUMN user_id BIGINT;

ALTER TABLE wards
    ADD CONSTRAINT uk_wards_user
        UNIQUE (user_id);

ALTER TABLE wards
    ADD CONSTRAINT fk_wards_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE;