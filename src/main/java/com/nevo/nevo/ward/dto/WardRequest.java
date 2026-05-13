package com.nevo.nevo.ward.dto;

import com.nevo.nevo.ward.entity.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class WardRequest {

    public record Create(
            @Positive(message = "키는 0보다 커야합니다")
            Double height,

            @Positive(message = "몸무게는 0보다 커야합니다")
            Double weight,

            @NotNull(message = "생년월일은 필수입니다.")
            @Past(message = "생년월일은 과거 날짜여야 합니다.")
            LocalDate birthDate,

            @NotNull(message = "성별은 필수입니다.")
            String gender
    ) {}

    public record UpsertPhysicalInfo(
            @Positive(message = "키는 0보다 커야합니다")
            Double height,

            @Positive(message = "몸무게는 0보다 커야합니다")
            Double weight,

            @NotNull(message = "생년월일은 필수입니다.")
            @Past(message = "생년월일은 과거 날짜여야 합니다.")
            LocalDate birthDate,

            @NotNull(message = "성별은 필수입니다.")
            Gender gender
    ) {}
}
