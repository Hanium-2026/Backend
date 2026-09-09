package com.nevo.nevo.session.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class SessionResponse {

    @Builder
    public record sessionInfo(
            Long sessionId,
            LocalDateTime startedAt
    ) {}

    @Builder
    public record Active(
            Long sessionId,
            LocalDateTime startedAt
    ) {}

    @Builder
    public record DataUpload(int saved, int skipped) {}

    // 분당 기록
    @Builder
    public record MinutePoint(
            Float avgScore,
            Float minScore,
            Float maxScore,
            Integer dangerCount,
            LocalDateTime minuteAt
            ) {}
}
