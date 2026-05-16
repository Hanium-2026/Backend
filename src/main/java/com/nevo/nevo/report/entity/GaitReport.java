package com.nevo.nevo.report.entity;

import com.nevo.nevo.session.entity.GaitSession;
import com.nevo.nevo.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gait_reports")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GaitReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private GaitSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "avg_score")
    private Float avgScore;

    @Column(name = "min_score")
    private Float minScore;

    @Column(name = "max_score")
    private Float maxScore;

    @Builder.Default
    @Column(name = "danger_count", nullable = false)
    private Integer dangerCount = 0;

    @Column(name = "variability_score")
    private Float variabilityScore;

    @Column(name = "asymmetry_score")
    private Float asymmetryScore;

    @Column(name = "report_summary")
    private String reportSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
