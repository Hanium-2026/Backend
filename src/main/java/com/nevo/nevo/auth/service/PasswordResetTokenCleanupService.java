package com.nevo.nevo.auth.service;

import com.nevo.nevo.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenCleanupService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // 매일 새벽 3시 만료된 비밀번호 재설정 토큰 자동 삭제
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredTokens() {
        int deleted = passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("[CleanUp] 만료된 비밀번호 재설정 토큰 {}건 삭제", deleted);
    }
}
