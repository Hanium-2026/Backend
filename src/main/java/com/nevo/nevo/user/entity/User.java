package com.nevo.nevo.user.entity;

import com.nevo.nevo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String phone;

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


    public static User from(String phone, String password, String name, Role role) {
        return User.builder()
                .phone(phone)
                .password(password)
                .name(name)
                .role(role)
                .build();
    }


    // 사용자 이름 수정 메서드
    public void updateName(String name) {
        this.name = name;
    }
    // 계정 탈퇴 (Soft delete) 처리 메서드

    /**
     * 탈퇴전: user_id=5, phone="01012345678", deleted_at=NULL
     * 탈퇴후: user_id=5, phone="deleted_5_01012345678", deleted_at=2026-09-07T10:30:00
     */
    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.phone = "deleted_" + this.id + "_" + this.phone; // 유니크 해제
        }

    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void clearFcmToken() {
        this.fcmToken = null;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}