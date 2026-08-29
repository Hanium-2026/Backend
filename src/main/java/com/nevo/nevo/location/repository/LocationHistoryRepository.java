package com.nevo.nevo.location.repository;

import com.nevo.nevo.location.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByWard_IdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long wardId, LocalDateTime start, LocalDateTime end);
}