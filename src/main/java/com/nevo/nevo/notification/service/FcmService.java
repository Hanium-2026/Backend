package com.nevo.nevo.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FcmService {

    @Async
    public void send(String token, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase 미초기화 — FCM 전송 스킵 (token: {})", token);
            return;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 (token: {}): {}", token, e.getMessage());
        }
    }
}