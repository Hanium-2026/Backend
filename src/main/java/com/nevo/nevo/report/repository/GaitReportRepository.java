package com.nevo.nevo.report.repository;

import com.nevo.nevo.report.entity.GaitReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GaitReportRepository extends JpaRepository<GaitReport, Long> {

    Optional<GaitReport> findBySession_Id(Long sessionId);
}
