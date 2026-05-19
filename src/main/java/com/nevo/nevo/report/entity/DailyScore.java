package com.nevo.nevo.report.entity;

import com.nevo.nevo.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_scores")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "avg_score")
    private Float avgScore;

    @Column(name = "min_score")
    private Float minScore;

    @Column(name = "max_score")
    private Float maxScore;

    @Builder.Default
    @Column(name = "session_count", nullable = false)
    private Integer sessionCount = 1;

    @Column(name = "variability_score")
    private Float variabilityScore;

    @Column(name = "asymmetry_score")
    private Float asymmetryScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addSession(Float newAvgScore, Float newMinScore, Float newMaxScore) {
        this.avgScore = ((this.avgScore * this.sessionCount) + newAvgScore) / (this.sessionCount + 1);
        this.minScore = Math.min(this.minScore, newMinScore);
        this.maxScore = Math.max(this.maxScore, newMaxScore);
        this.sessionCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
