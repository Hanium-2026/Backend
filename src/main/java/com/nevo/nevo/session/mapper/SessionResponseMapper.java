package com.nevo.nevo.session.mapper;

import com.nevo.nevo.session.dto.response.SessionResponse;

import java.time.LocalDateTime;

public class SessionResponseMapper {

    public static SessionResponse.sessionInfo toSessionInfoResponse(Long sessionId, LocalDateTime startedAt) {
        return SessionResponse.sessionInfo.builder()
                .sessionId(sessionId)
                .startedAt(startedAt)
                .build();
    }
}
