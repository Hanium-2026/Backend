-- ward당 최신 1건만 유지하기 위한 UNIQUE 제약
-- 기존 데이터가 있을 경우 ward_id별 최신 1건만 남기고 삭제
DELETE FROM locations l1
USING locations l2
WHERE l1.ward_id = l2.ward_id
  AND l1.location_id < l2.location_id;

ALTER TABLE locations
    ADD CONSTRAINT uq_locations_ward_id UNIQUE (ward_id);