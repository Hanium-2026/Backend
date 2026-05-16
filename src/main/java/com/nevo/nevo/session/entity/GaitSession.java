package com.nevo.nevo.session.entity;

import com.nevo.nevo.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gait_sessions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GaitSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Builder.Default
    @Column(name = "stroke_detected", nullable = false)
    private Boolean strokeDetected = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public void complete(LocalDateTime endedAt) {
        this.status = SessionStatus.COMPLETED;
        this.endedAt = endedAt;
    }

    public void markStrokeDanger() {
        this.strokeDetected = true;
    }

}
