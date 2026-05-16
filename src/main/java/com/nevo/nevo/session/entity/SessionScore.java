package com.nevo.nevo.session.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_scores")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GaitSession session;

    @Column(name = "minute_at", nullable = false)
    private LocalDateTime minuteAt;

    @Column(name = "avg_score", nullable = false)
    private Float avgScore;

    @Column(name = "min_score", nullable = false)
    private Float minScore;

    @Column(name = "max_score", nullable = false)
    private Float maxScore;

    @Builder.Default
    @Column(name = "danger_count", nullable = false)
    private Integer dangerCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
