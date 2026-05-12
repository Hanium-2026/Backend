package com.nevo.nevo.auth.jwt;

// SecurityContext에 저장되는 인증 정보 — JWT Claims에서 추출한 값
public record JwtAuthentication(
        Long userId,
        Long wardId,  // GUARDIAN이면 null
        String role
) {}
