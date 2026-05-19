package com.nevo.nevo.report.repository;

import com.nevo.nevo.report.entity.DailyScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyScoreRepository extends JpaRepository<DailyScore, Long> {

    Optional<DailyScore> findByWard_IdAndDate(Long wardId, LocalDate date);

    List<DailyScore> findByWard_IdAndDateBetweenOrderByDateAsc(Long wardId, LocalDate from, LocalDate to);

    // 여러 ward의 7일치 daily_scores를 IN 쿼리 1번에 조회 (보호자 대시보드 N+1 방지)
    List<DailyScore> findByWard_IdInAndDateBetweenOrderByDateAsc(List<Long> wardIds, LocalDate from, LocalDate to);
}
