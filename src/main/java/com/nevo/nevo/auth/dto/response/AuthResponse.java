package com.nevo.nevo.auth.dto.response;

import lombok.Builder;

public class AuthResponse {

    @Builder
    public record SignUp(
            String accessToken,
            String refreshToken,
            String role
    ) {}
}
