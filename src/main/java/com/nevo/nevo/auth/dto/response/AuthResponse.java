package com.nevo.nevo.auth.dto.response;

import lombok.Builder;

public class AuthResponse {

    @Builder
    public record Token(
            String accessToken,
            String refreshToken,
            String role
    ) {}
}
