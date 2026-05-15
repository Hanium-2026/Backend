package com.nevo.nevo.auth.entity;

import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
