package com.nevo.nevo.ward.entity;

import com.nevo.nevo.global.entity.BaseEntity;
import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ward_guardian_link")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WardGuardianLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_user_id", nullable = false)
    private User guardian;
}
