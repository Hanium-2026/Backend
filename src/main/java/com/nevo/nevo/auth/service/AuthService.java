package com.nevo.nevo.auth.service;

import com.nevo.nevo.auth.dto.request.AuthRequest;
import com.nevo.nevo.auth.dto.response.AuthResponse;
import com.nevo.nevo.auth.entity.RefreshToken;
import com.nevo.nevo.auth.exception.code.AuthErrorCode;
import com.nevo.nevo.auth.jwt.JwtUtil;
import com.nevo.nevo.auth.repository.RefreshTokenRepository;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.user.entity.Consent;
import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.user.entity.User;
import com.nevo.nevo.user.repository.ConsentRepository;
import com.nevo.nevo.user.repository.UserRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final ConsentRepository consentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp request) {
        // 1. 이메일 중복 확인 (탈퇴하지 않은 사용자 기준)
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new CustomException(AuthErrorCode.EMAIL_DUPLICATED);
        }

        // 2. User 저장
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role())
                .build();
        userRepository.save(user);

        // 3. WARD이면 Ward 저장
        Long wardId = null; // 역할이 GUARDIAN이면 wardId는 없어야 함.
        if (request.role() == Role.WARD) {
            Ward ward = Ward.builder()
                    .user(user)
                    .height(request.height())
                    .weight(request.weight())
                    .birthDate(request.birthDate())
                    .gender(request.gender())
                    .build();
            wardRepository.save(ward);
            wardId = ward.getId();
        }

        // 4. Consent 목록 저장
        List<Consent> consents = request.consents().stream()
                .map(item -> Consent.builder()
                        .user(user)
                        .consentType(item.consentType())
                        .agreed(item.agreed())
                        .agreedAt(item.agreed() ? LocalDateTime.now() : null)
                        .build())
                .toList();
        consentRepository.saveAll(consents);

        // 5. JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getId(), wardId, request.role().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 6. RefreshToken 해시 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshToken))
                .deviceId(request.deviceId())
                .build());

        return AuthResponse.SignUp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(request.role().name())
                .build();
    }


    @Transactional
    public AuthResponse.Login login(AuthRequest.Login request) {
        // 1. 이메일로 사용자 조회 (탈퇴 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증 (이메일/비밀번호 어느 쪽이 틀렸는지 노출 금지)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 3. WARD면 wardId 조회, GUARDIAN이면 null
        Long wardId = null;
        if (user.getRole() == Role.WARD) {
            wardId = wardRepository.findByUser_Id(user.getId())
                    .map(Ward::getId)
                    .orElse(null);
        }

        // 4. JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getId(), wardId, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. 동일 device 기존 토큰 전체 revoke (비정상 상황으로 복수 존재 시에도 안전)
        refreshTokenRepository.findAllByUserIdAndDeviceIdAndRevokedFalse(user.getId(), request.deviceId())
                .forEach(RefreshToken::revoke);

        // 6. 새 RefreshToken 해시 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshToken))
                .deviceId(request.deviceId())
                .build());

        return AuthResponse.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
