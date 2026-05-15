package com.nevo.nevo.location.repository;

import com.nevo.nevo.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByWard_Id(Long wardId);

    @Modifying
    @Query(value = """
            INSERT INTO locations (ward_id, latitude, longitude, recorded_at)
            VALUES (:wardId, :latitude, :longitude, NOW())
            ON CONFLICT (ward_id) DO UPDATE SET
                latitude   = EXCLUDED.latitude,
                longitude  = EXCLUDED.longitude,
                recorded_at = EXCLUDED.recorded_at
            """, nativeQuery = true)
    void upsertLocation(@Param("wardId") Long wardId,
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude);
}