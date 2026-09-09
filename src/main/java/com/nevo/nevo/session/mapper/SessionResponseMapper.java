package com.nevo.nevo.session.mapper;

import com.nevo.nevo.session.dto.response.SessionResponse;

import java.time.LocalDateTime;

public class SessionResponseMapper {

    public static SessionResponse.Start toStartResponse(Long sessionId, LocalDateTime startedAt) {
        return SessionResponse.Start.builder()
                .sessionId(sessionId)
                .startedAt(startedAt)
                .build();
    }
}
