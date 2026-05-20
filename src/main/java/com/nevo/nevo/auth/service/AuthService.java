package com.nevo.nevo.auth.service;

import com.nevo.nevo.auth.dto.request.AuthRequest;
import com.nevo.nevo.auth.dto.request.AuthRequest.ConsentItem;
import com.nevo.nevo.auth.dto.response.AuthResponse;
import com.nevo.nevo.auth.entity.RefreshToken;
import com.nevo.nevo.auth.entity.SmsVerificationPurpose;
import com.nevo.nevo.auth.exception.code.AuthErrorCode;
import com.nevo.nevo.auth.jwt.JwtUtil;
import com.nevo.nevo.auth.repository.RefreshTokenRepository;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.user.entity.Consent;
import com.nevo.nevo.user.entity.ConsentType;
import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.user.entity.User;
import com.nevo.nevo.user.repository.ConsentRepository;
import com.nevo.nevo.user.repository.UserRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final ConsentRepository consentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // SMS OTP 발송 - POST /api/auth/sms/send
    // @Async는 SmsService 내부에서 처리
    public void sendSms(String phone, SmsVerificationPurpose purpose) {
        smsService.sendOtp(phone, purpose);
    }

    // SMS OTP 인증 - POST /api/auth/sms/verify
    // 인증 성공 시 Redis에 verified 상태 저장 (10분 유지)
    public void verifySms(String phone, String code, SmsVerificationPurpose purpose) {
        smsService.verifyOtp(phone, purpose, code);
    }

    // 회원가입 - POST /api/auth/sign-up
    // /sms/verify(SIGNUP) 완료 후 호출해야 함
    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp request) {
        // 1. 전화번호 인증 완료 여부 확인 (미인증 시 예외)
        smsService.consumeVerified(request.phone(), SmsVerificationPurpose.SIGNUP);

        // 2. 전화번호 중복 확인 (탈퇴하지 않은 사용자 기준)
        if (userRepository.existsByPhoneAndDeletedAtIsNull(request.phone())) {
            throw new CustomException(AuthErrorCode.PHONE_DUPLICATED);
        }

        // 3. WARD 필수 필드 사전 검증
        if (request.role() == Role.WARD) {
            if (request.height() == null || request.weight() == null
                    || request.birthDate() == null || request.gender() == null) {
                throw new CustomException(AuthErrorCode.WARD_FIELDS_REQUIRED);
            }
        }

        // 4. 필수 약관 동의 검증 (TERMS, PRIVACY는 agreed=true 필수)
        Set<ConsentType> requiredConsents = Set.of(ConsentType.TERMS, ConsentType.PRIVACY);
        Map<ConsentType, Boolean> consentMap = request.consents().stream()
                .collect(Collectors.toMap(ConsentItem::consentType, ConsentItem::agreed));
        for (ConsentType type : requiredConsents) {
            if (!Boolean.TRUE.equals(consentMap.get(type))) {
                throw new CustomException(AuthErrorCode.REQUIRED_CONSENT_NOT_AGREED);
            }
        }

        // 5. User 저장 (동시 요청 레이스 컨디션: unique constraint 위반 시 PHONE_DUPLICATED 반환)
        User user;
        try {
            user = userRepository.save(User.builder()
                    .phone(request.phone())
                    .password(passwordEncoder.encode(request.password()))
                    .name(request.name())
                    .role(request.role())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(AuthErrorCode.PHONE_DUPLICATED);
        }

        // 6. WARD이면 Ward 저장
        Long wardId = null;
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

        // 7. Consent 목록 저장
        List<Consent> consents = request.consents().stream()
                .map(item -> Consent.builder()
                        .user(user)
                        .consentType(item.consentType())
                        .agreed(item.agreed())
                        .agreedAt(item.agreed() ? LocalDateTime.now() : null)
                        .build())
                .toList();
        consentRepository.saveAll(consents);

        // 8. JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getId(), wardId, request.role().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 9. RefreshToken 해시 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .deviceId(request.deviceId())
                .build());

        return AuthResponse.SignUp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(request.role().name())
                .build();
    }

    // 로그인 - POST /api/auth/login
    @Transactional
    public AuthResponse.Login login(AuthRequest.Login request) {
        // 1. 전화번호로 사용자 조회 (탈퇴 제외)
        User user = userRepository.findByPhoneAndDeletedAtIsNull(request.phone())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증 (전화번호/비밀번호 어느 쪽이 틀렸는지 노출 금지)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 3. WARD면 wardId 조회, GUARDIAN이면 null
        Long wardId = null;
        if (user.getRole() == Role.WARD) {
            wardId = wardRepository.findByUser_Id(user.getId())
                    .map(Ward::getId)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.WARD_NOT_FOUND));
        }

        // 4. JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getId(), wardId, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. 동일 device 기존 토큰 전체 revoke (비정상 상황으로 복수 존재 시에도 안전)
        refreshTokenRepository.findAllByUser_IdAndDeviceIdAndRevokedFalse(user.getId(), request.deviceId())
                .forEach(RefreshToken::revoke);

        // 6. 디바이스 수 제한: 활성 토큰이 5개 이상이면 오래된 것부터 revoke
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findAllByUser_IdAndRevokedFalseOrderByIdAsc(user.getId());
        if (activeTokens.size() >= 5) {
            activeTokens.subList(0, activeTokens.size() - 4).forEach(RefreshToken::revoke);
        }

        // 7. 새 RefreshToken 해시 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .deviceId(request.deviceId())
                .build());

        return AuthResponse.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    // 로그아웃 - POST /api/auth/logout
    @Transactional
    public void logout(AuthRequest.Logout request) {
        String hash = hashToken(request.refreshToken());

        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_TOKEN));

        if (token.getRevoked() || token.getUsed()) {
            throw new CustomException(AuthErrorCode.REVOKED_TOKEN);
        }

        token.revoke();
    }

    // 토큰 갱신 - POST /api/auth/refresh
    @Transactional
    public AuthResponse.Refresh refresh(AuthRequest.Refresh request) {
        // 1. JWT 서명·만료 검증
        jwtUtil.parseClaims(request.refreshToken());

        // 2. DB에서 유효한 토큰 조회 (revoked=false, used=false)
        String hash = hashToken(request.refreshToken());
        RefreshToken oldToken = refreshTokenRepository.findByTokenHashAndRevokedFalseAndUsedFalse(hash)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_TOKEN));

        User user = oldToken.getUser();

        // 3. WARD면 wardId 조회, GUARDIAN이면 null
        Long wardId = null;
        if (user.getRole() == Role.WARD) {
            wardId = wardRepository.findByUser_Id(user.getId())
                    .map(Ward::getId)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.WARD_NOT_FOUND));
        }

        // 4. 기존 토큰 used 처리 (재사용 공격 방지)
        oldToken.markUsed();

        // 5. 새 토큰 발급 및 저장
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), wardId, user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRefreshToken))
                .deviceId(oldToken.getDeviceId())
                .build());

        return AuthResponse.Refresh.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole().name())
                .build();
    }

    // 비밀번호 재설정 요청 - POST /api/auth/password-reset/request
    // 전화번호 존재 여부와 무관하게 항상 성공 응답 (사용자 존재 여부 노출 방지)
    public void requestPasswordReset(String phone) {
        userRepository.findByPhoneAndDeletedAtIsNull(phone)
                .ifPresent(user -> smsService.sendOtp(phone, SmsVerificationPurpose.PASSWORD_RESET));
    }

    // 비밀번호 재설정 확인 - POST /api/auth/password-reset/confirm
    // /sms/verify(PASSWORD_RESET) 완료 후 호출해야 함
    @Transactional
    public void confirmPasswordReset(AuthRequest.PasswordResetConfirm request) {
        // 1. 인증 완료 여부 확인
        smsService.consumeVerified(request.phone(), SmsVerificationPurpose.PASSWORD_RESET);

        // 2. 사용자 조회
        User user = userRepository.findByPhoneAndDeletedAtIsNull(request.phone())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        // 3. 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(request.newPassword()));

        // 4. 비밀번호 변경 후 전체 세션 강제 로그아웃
        refreshTokenRepository.deleteByUser_Id(user.getId());

        // 5. 비밀번호 변경 알림 발송 (공격자에 의한 변경 감지용)
        smsService.sendPasswordChangedNotification(request.phone());
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
