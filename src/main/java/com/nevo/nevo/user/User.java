package com.nevo.nevo.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 1. 계정 탈퇴 (Soft delete) 처리
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 2. FCM 토큰 업데이트
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}