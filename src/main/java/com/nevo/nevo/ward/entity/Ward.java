package com.nevo.nevo.ward.entity;

import com.nevo.nevo.global.entity.BaseEntity;
import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "wards")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    // ward 생성 매서드
    public static Ward from(User user, Double height, Double weight, LocalDate birthDate, Gender gender) {
        return Ward.builder()
                .user(user)
                .height(height)
                .weight(weight)
                .birthDate(birthDate)
                .gender(gender)
                .build();
    }

    public void updatePhysicalInfo(Double height, Double weight, LocalDate birthDate, Gender gender) {
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
