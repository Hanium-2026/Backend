package com.nevo.nevo.auth.service;

import com.nevo.nevo.auth.dto.request.AuthRequest;
import com.nevo.nevo.auth.dto.request.AuthRequest.ConsentItem;
import com.nevo.nevo.auth.dto.response.AuthResponse;
import com.nevo.nevo.auth.entity.RefreshToken;
import com.nevo.nevo.auth.entity.SmsVerificationPurpose;
import com.nevo.nevo.auth.exception.code.AuthErrorCode;
import com.nevo.nevo.auth.jwt.JwtUtil;
import com.nevo.nevo.auth.mapper.AuthResponseMapper;
import com.nevo.nevo.auth.repository.RefreshTokenRepository;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.user.entity.Consent;
import com.nevo.nevo.user.entity.ConsentType;
import com.nevo.nevo.user.entity.User;
import com.nevo.nevo.user.exception.code.UserErrorCode;
import com.nevo.nevo.user.repository.ConsentRepository;
import com.nevo.nevo.user.repository.UserRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static com.nevo.nevo.user.entity.Role.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public AuthResponse.Token signUp(AuthRequest.SignUp request) {

        log.info("[회원가입] 회원가입 호출");

        // 전화번호 인증 완료 여부 확인 (미인증 시 예외)
        smsService.consumeVerified(request.phone(), SmsVerificationPurpose.SIGNUP);

        // 전화번호 중복 확인 (탈퇴하지 않은 사용자 기준)
        if (userRepository.existsByPhoneAndDeletedAtIsNull(request.phone())) {
            log.warn("[회원가입] 이미 존재하는 전화번호입니다.");
            throw new CustomException(AuthErrorCode.PHONE_DUPLICATED);
        }

        // WARD 필수 필드 사전 검증
        if (request.role() == WARD) {
            if (request.height() == null || request.weight() == null
                    || request.birthDate() == null || request.gender() == null) {
                log.warn("[회원가입] 필수 항목들을 입력해주세요.");
                throw new CustomException(AuthErrorCode.WARD_FIELDS_REQUIRED);
            }
        }

        // 필수 약관 동의 검증 (TERMS, PRIVACY는 agreed=true 필수)
        Set<ConsentType> requiredConsents = Set.of(ConsentType.TERMS, ConsentType.PRIVACY);

        Map<ConsentType, Boolean> consentMap = request.consents().stream()
                .collect(Collectors.toMap(ConsentItem::consentType, ConsentItem::agreed));

        for (ConsentType type : requiredConsents) {
            if (!Boolean.TRUE.equals(consentMap.get(type))) {
                log.warn("[회원가입] 이용약관, 개인정보 처리방침은 필수 항목입니다.");
                throw new CustomException(AuthErrorCode.REQUIRED_CONSENT_NOT_AGREED);
            }
        }

        // User 저장 (동시 요청 레이스 컨디션: unique constraint 위반 시 PHONE_DUPLICATED 반환)
        User user;
        try {

            user = User.from(
                    request.phone(), passwordEncoder.encode(request.password()), request.name(), request.role()
            );

            // 즉시 flush
            userRepository.saveAndFlush(user);

        } catch (DataIntegrityViolationException e) {
                log.warn("[회원가입] 동시요청: {}", e.getMessage());
                throw new CustomException(AuthErrorCode.PHONE_DUPLICATED);
        }

        // WARD이면 Ward 저장
        Long wardId = null;
        if (request.role() == WARD) {

            Ward ward = Ward.from(
                    user, request.height(), request.weight(), request.birthDate(), request.gender()
            );

            wardRepository.save(ward);
            wardId = ward.getId();
        }

        // Consent 목록 저장
        List<Consent> consents = request.consents().stream()
                .map(item ->
                        Consent.from(
                                user, item.consentType(), item.agreed(), (item.agreed() ? LocalDateTime.now() : null)
                )).toList();

        consentRepository.saveAll(consents);

        // JWT 발급
        String accessTokenString = jwtUtil.generateAccessToken(user.getId(), wardId, request.role().name());
        String refreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        // RefreshToken 해시 저장
        RefreshToken refreshToken = RefreshToken.create(user, hashToken(refreshTokenString), request.deviceId());
        refreshTokenRepository.save(refreshToken);

        log.info("[회원가입] 회원가입 완료");

        return AuthResponseMapper
                .toTokenResponse(accessTokenString, refreshTokenString, request.role().name());
    }

    // 로그인 - POST /api/auth/login
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {

        log.info("[로그인] 로그인  호출");

        // 전화번호로 사용자 조회 (탈퇴 제외)
        User user = userRepository.findByPhoneAndDeletedAtIsNull(request.phone())
                .orElseThrow(() -> {
                    log.warn("[로그인] 전화번호 또는 비밀번호가 일치하지 않습니다.");
                    return new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
                });

        // 비밀번호 검증 (전화번호/비밀번호 어느 쪽이 틀렸는지 노출 금지)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("[로그인] 전화번호 또는 비밀번호가 일치하지 않습니다.");
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // WARD면 wardId 조회, GUARDIAN이면 null
        Long wardId = null;
        if (user.getRole() == WARD) {
            wardId = wardRepository.findByUser_Id(user.getId())
                    .map(Ward::getId)
                    .orElseThrow(() -> {
                        log.warn("[로그인] WARD를 찾을 수 없습니다. userId = {}", user.getId());
                        return new CustomException(AuthErrorCode.WARD_NOT_FOUND);
                    });
        }

        // JWT 발급
        String accessTokenString = jwtUtil.generateAccessToken(user.getId(), wardId, user.getRole().name());
        String refreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        // 동일 device 기존 토큰 전체 revoke (비정상 상황으로 복수 존재 시에도 안전)
        refreshTokenRepository
                .findAllByUser_IdAndDeviceIdAndRevokedFalse(user.getId(), request.deviceId())
                .forEach(RefreshToken::revoke);

        // 디바이스 수 제한: 활성 토큰이 5개 이상이면 오래된 것부터 revoke
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findAllActiveRefreshToken(user.getId(), LocalDateTime.now());

        if (activeTokens.size() >= 5) {
            activeTokens.subList(0, activeTokens.size() - 4).forEach(RefreshToken::revoke);
        }

        // 새 RefreshToken 해시 저장
        RefreshToken refreshToken = RefreshToken.create(user, hashToken(refreshTokenString), request.deviceId());
        refreshTokenRepository.save(refreshToken);

        log.info("[로그인] 로그인 완료");

        return AuthResponseMapper
                .toTokenResponse(accessTokenString, refreshTokenString, user.getRole().name());
    }

    // 로그아웃 - POST /api/auth/logout
    @Transactional
    public void logout(AuthRequest.Logout request) {

        log.info("[로그아웃] 로그아웃 호출");
        String hash = hashToken(request.refreshToken());

        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("[로그아웃] 유효하지 않은 토큰입니다.");
                    return new CustomException(AuthErrorCode.INVALID_TOKEN);
                });

        if (token.getRevoked() || token.getUsed()) {
            log.warn("[로그아웃] 이미 무효화되거나 만료된 토큰입니다.");
            throw new CustomException(AuthErrorCode.REVOKED_TOKEN);
        }

        token.revoke();
        log.info("[로그아웃] 로그아웃 완료");
    }

    // 토큰 갱신 - POST /api/auth/refresh
    @Transactional
    public AuthResponse.Token refresh(AuthRequest.Refresh request) {

        log.info("[토큰 갱신] 토큰 갱신 호출");

        // JWT 서명·만료 검증
        jwtUtil.parseClaims(request.refreshToken());

        // 유효한 토큰 조회 (revoked=false, used=false)
        String hash = hashToken(request.refreshToken());

        RefreshToken oldToken = refreshTokenRepository.findByActiveTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("[토큰 갱신] 유효하지 않은 토큰입니다.");
                    return new CustomException(AuthErrorCode.INVALID_TOKEN);
                });

        User user = oldToken.getUser();

        // WARD면 wardId 조회, GUARDIAN이면 null
        Long wardId = null;
        if (user.getRole() == WARD) {
            wardId = wardRepository.findByUser_Id(user.getId())
                    .map(Ward::getId)
                    .orElseThrow(() -> {
                        log.warn("[토큰 갱신] WARD를 찾을 수 없습니다. userId = {}", user.getId());
                        return new CustomException(AuthErrorCode.WARD_NOT_FOUND);
                    });
        }

        // 기존 토큰 used 처리 (재사용 공격 방지)
        oldToken.markUsed();

        // 새 토큰 발급 및 저장
        String newAccessTokenString = jwtUtil
                .generateAccessToken(user.getId(), wardId, user.getRole().name());

        String newRefreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken
                .create(user, hashToken(newRefreshTokenString), oldToken.getDeviceId());

        refreshTokenRepository.save(refreshToken);

        log.info("[토큰 갱신] 토큰 갱신 완료");
        return AuthResponseMapper
                .toTokenResponse(newAccessTokenString, newRefreshTokenString, user.getRole().name());
    }

    // 비밀번호 재설정 요청 - POST /api/auth/password-reset/request
    // 전화번호 존재 여부와 무관하게 항상 성공 응답 (사용자 존재 여부 노출 방지)
    public void requestPasswordReset(String phone) {

        log.debug("[비밀번호 재설정] 비밀번호 재설정 시작 phone = {}", phone);

        userRepository.findByPhoneAndDeletedAtIsNull(phone)
                .ifPresent(user -> smsService.sendOtp(phone, SmsVerificationPurpose.PASSWORD_RESET));
    }

    // 비밀번호 재설정 확인 - POST /api/auth/password-reset/confirm
    // /sms/verify(PASSWORD_RESET) 완료 후 호출해야 함
    @Transactional
    public void confirmPasswordReset(AuthRequest.PasswordResetConfirm request) {
        // 인증 완료 여부 확인
        smsService.consumeVerified(request.phone(), SmsVerificationPurpose.PASSWORD_RESET);

        // 사용자 조회
        User user = userRepository.findByPhoneAndDeletedAtIsNull(request.phone())
                .orElseThrow(() -> {
                    log.warn("[비밀번호 재설정 확인] 사용자를 찾을 수 없습니다");
                    return new CustomException(UserErrorCode.USER_NOT_FOUND);}
                );

        // 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(request.newPassword()));

        // 비밀번호 변경 후 전체 세션 강제 로그아웃
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // 비밀번호 변경 알림 발송
        smsService.sendPasswordChangedNotification(request.phone());
    }

    // 토근값 SHA-256 해시
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
