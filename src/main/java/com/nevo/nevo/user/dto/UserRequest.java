package com.nevo.nevo.user.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    public record UpdateName(
            @NotBlank(message = "이름은 비어있을 수 없습니다.")
            String name
    ) {}

    public record UpdateFcmToken(
            @NotBlank(message = "FCM 토큰 값이 필요합니다.")
            String fcmToken
    ) {}
}