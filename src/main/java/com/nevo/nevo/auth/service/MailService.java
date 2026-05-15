package com.nevo.nevo.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 비밀번호 재설정 이메일 발송 - POST /api/auth/password-reset/request 에서 호출
    // @Async: SMTP 응답 대기로 인한 API 지연 방지를 위해 별도 스레드에서 비동기 처리
    @Async
    public void sendPasswordResetEmail(String to, String rawToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[NEVO] 비밀번호 재설정");
        message.setText(
                "비밀번호 재설정 토큰: " + rawToken + "\n\n" +
                "이 토큰은 15분 후 만료됩니다.\n" +
                "본인이 요청하지 않았다면 이 메일을 무시하세요."
        );
        mailSender.send(message);
    }
}
