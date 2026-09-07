package com.nevo.nevo.auth.mapper;

import com.nevo.nevo.auth.dto.response.AuthResponse;

public class AuthResponseMapper {

    public static AuthResponse.Token toTokenResponse(String accessToken, String refreshToken, String role) {
        return AuthResponse.Token
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }
}
