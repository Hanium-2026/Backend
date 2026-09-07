package com.nevo.nevo.auth.entity;

import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.sql.*;
import java.util.Scanner;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void revoke() {
        this.revoked = true;
    }

    public void markUsed() {
        this.used = true;
    }

    public static RefreshToken create(User user, String tokenHash, String deviceId) {
        return RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .deviceId(deviceId)
                .build();
    }
}
