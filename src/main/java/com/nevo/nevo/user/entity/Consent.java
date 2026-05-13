package com.nevo.nevo.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
