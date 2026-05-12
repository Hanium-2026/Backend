package com.nevo.nevo.user.dto.requestDto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank(message = "FCM 토큰 값이 필요합니다.")
        String fcmToken
) {
}
