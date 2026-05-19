package com.nevo.nevo.report.repository;

import com.nevo.nevo.report.entity.GaitReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GaitReportRepository extends JpaRepository<GaitReport, Long> {

    Optional<GaitReport> findBySession_Id(Long sessionId);

    List<GaitReport> findAllByWard_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long wardId, LocalDateTime from, LocalDateTime to);

    // 각 ward당 가장 최근 분석 결과 1건씩 IN 쿼리 1번에 조회 (보호자 대시보드 N+1 방지)
    @Query(value = """
            SELECT DISTINCT ON (ward_id) *
            FROM gait_reports
            WHERE ward_id IN (:wardIds)
            ORDER BY ward_id, created_at DESC
            """, nativeQuery = true)
    List<GaitReport> findLatestByWardIdIn(@Param("wardIds") List<Long> wardIds);
}
