package com.nevo.nevo.auth.entity;

import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static PasswordResetToken create(User user, String tokenHash, LocalDateTime expiresAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.user = user;
        t.tokenHash = tokenHash;
        t.expiresAt = expiresAt;
        t.used = false;
        t.createdAt = LocalDateTime.now();
        return t;
    }

    public void markUsed() {
        this.used = true;
    }
}
