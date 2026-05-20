package com.nevo.nevo.auth.service;

import com.nevo.nevo.auth.entity.SmsVerificationPurpose;
import com.nevo.nevo.auth.exception.code.AuthErrorCode;
import com.nevo.nevo.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RedisTemplate<String, String> redisTemplate;

    // OTP 유효 시간: 5분
    private static final long OTP_TTL_SECONDS = 300;
    // 인증 완료 상태 유효 시간: 10분 (sign-up 또는 password-reset/confirm 호출 전까지 유지)
    private static final long VERIFIED_TTL_SECONDS = 600;
    // OTP 최대 시도 횟수 및 잠금 시간: 5회 초과 시 15분 잠금
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long ATTEMPTS_TTL_SECONDS = 900;

    // Redis Key 형식
    // OTP:      "sms:{PURPOSE}:{phone}"           예: "sms:SIGNUP:01012345678"
    // 인증완료:  "sms:{PURPOSE}:verified:{phone}"  예: "sms:SIGNUP:verified:01012345678"
    private String otpKey(String phone, SmsVerificationPurpose purpose) {
        return "sms:" + purpose.name() + ":" + phone;
    }

    private String verifiedKey(String phone, SmsVerificationPurpose purpose) {
        return "sms:" + purpose.name() + ":verified:" + phone;
    }

    private String attemptsKey(String phone, SmsVerificationPurpose purpose) {
        return "otp:attempts:" + purpose.name() + ":" + phone;
    }

    // OTP 생성 및 Redis 저장 (TTL 5분)
    // @Async: SMS 발송 지연이 API 응답 시간에 영향을 주지 않도록 비동기 처리
    // 재발송 시 기존 키를 덮어써서 이전 OTP 자동 무효화
    @Async
    public void sendOtp(String phone, SmsVerificationPurpose purpose) {
        String code = generateOtp();
        redisTemplate.opsForValue().set(otpKey(phone, purpose), code, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        // TODO: 네이버 클라우드 SMS API 연동 — 개발 단계에서는 로그로 대체
        log.info("[SMS][DEV ONLY] phone={}, purpose={}, code={}", phone, purpose, code);
    }

    // OTP 검증
    // 일치하면: OTP 키 삭제 + verified 키 저장 (10분 유지) + 시도 횟수 초기화
    // 불일치:   시도 횟수 증가, 5회 초과 시 OTP_MAX_ATTEMPTS 에러 (15분 잠금)
    // 만료:     OTP_EXPIRED 에러
    public void verifyOtp(String phone, SmsVerificationPurpose purpose, String inputCode) {
        // 시도 횟수 확인 및 증가
        String attemptsKey = attemptsKey(phone, purpose);
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, ATTEMPTS_TTL_SECONDS, TimeUnit.SECONDS);
        }
        if (attempts > MAX_OTP_ATTEMPTS) {
            throw new CustomException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        String storedCode = redisTemplate.opsForValue().get(otpKey(phone, purpose));

        if (storedCode == null) {
            throw new CustomException(AuthErrorCode.OTP_EXPIRED);
        }
        if (!storedCode.equals(inputCode)) {
            throw new CustomException(AuthErrorCode.OTP_INVALID);
        }

        // 인증 완료 처리
        redisTemplate.delete(otpKey(phone, purpose));                                                                  // OTP 즉시 삭제 (재사용 방지)
        redisTemplate.delete(attemptsKey);                                                                             // 시도 횟수 초기화
        redisTemplate.opsForValue().set(verifiedKey(phone, purpose), "true", VERIFIED_TTL_SECONDS, TimeUnit.SECONDS); // 인증 완료 상태 저장
    }

    // 인증 완료 여부 확인 후 키 삭제
    // sign-up, password-reset/confirm에서 호출
    // 인증 미완료이면 PHONE_NOT_VERIFIED 에러
    public void consumeVerified(String phone, SmsVerificationPurpose purpose) {
        String key = verifiedKey(phone, purpose);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new CustomException(AuthErrorCode.PHONE_NOT_VERIFIED);
        }
        redisTemplate.delete(key); // 사용 후 즉시 삭제 (재사용 방지)
    }

    // 비밀번호 변경 알림 발송 (비동기)
    @Async
    public void sendPasswordChangedNotification(String phone) {
        // TODO: 네이버 클라우드 SMS API 연동 시 실제 발송으로 교체
        log.info("[SMS][DEV ONLY] 비밀번호 변경 알림 발송 → phone={}", phone);
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
