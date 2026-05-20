-- 기존 이메일 기반 계정은 전화번호 기반으로 전환 시 사용 불가 → 테스트 데이터 삭제
TRUNCATE TABLE users CASCADE;

ALTER TABLE users DROP COLUMN email;
ALTER TABLE users ADD COLUMN phone VARCHAR(20) NOT NULL UNIQUE;
