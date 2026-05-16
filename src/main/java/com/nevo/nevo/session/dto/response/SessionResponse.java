package com.nevo.nevo.session.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class SessionResponse {

    @Builder
    public record Start(
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
}
