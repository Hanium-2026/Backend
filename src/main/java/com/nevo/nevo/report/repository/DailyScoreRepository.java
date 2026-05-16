package com.nevo.nevo.report.repository;

import com.nevo.nevo.report.entity.DailyScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyScoreRepository extends JpaRepository<DailyScore, Long> {

    Optional<DailyScore> findByWard_IdAndDate(Long wardId, LocalDate date);

    List<DailyScore> findByWard_IdAndDateBetweenOrderByDateAsc(Long wardId, LocalDate from, LocalDate to);
}
