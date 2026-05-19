package com.nevo.nevo.notification.dto.response;

import com.nevo.nevo.notification.entity.Alert;

import java.time.LocalDateTime;

public class AlertResponse {

    public record AlertInfo(
            Long alertId,
            String type,
            Long sessionId,
            String message,
            LocalDateTime createdAt
    ) {
        public static AlertInfo from(Alert alert) {
            return new AlertInfo(
                    alert.getId(),
                    alert.getType().name(),
                    alert.getSessionId(),
                    alert.getMessage(),
                    alert.getCreatedAt()
            );
        }
    }
}