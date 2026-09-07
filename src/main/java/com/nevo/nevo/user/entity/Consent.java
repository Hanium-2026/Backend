package com.nevo.nevo.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false)
    private ConsentType consentType;

    @Column(nullable = false)
    private Boolean agreed;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;


    public static Consent from(
            User user, ConsentType consentType, Boolean agreed, LocalDateTime agreedAt
    ) {
        return Consent.builder()
                .user(user)
                .consentType(consentType)
                .agreed(agreed)
                .agreedAt(agreedAt)
                .build();
    }
}
