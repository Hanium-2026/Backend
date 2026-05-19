package com.nevo.nevo.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) return;

        ClassPathResource resource = new ClassPathResource(credentialsPath);
        if (!resource.exists()) {
            log.warn("Firebase 서비스 계정 파일({})이 없습니다. FCM 기능이 비활성화됩니다.", credentialsPath);
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료");
        } catch (IOException e) {
            log.warn("Firebase 초기화 실패: {}. FCM 기능이 비활성화됩니다.", e.getMessage());
        }
    }
}