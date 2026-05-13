package com.nevo.nevo.ward.entity;

import com.nevo.nevo.global.entity.BaseEntity;
import com.nevo.nevo.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "wards")
@Getter
@NoArgsConstructor
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

    public void updatePhysicalInfo(Double height, Double weight, LocalDate birthDate, Gender gender) {
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
