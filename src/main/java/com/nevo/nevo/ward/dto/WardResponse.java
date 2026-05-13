package com.nevo.nevo.ward.dto;

import com.nevo.nevo.ward.entity.Gender;
import com.nevo.nevo.ward.entity.Ward;

import java.time.LocalDate;

public class WardResponse {

    public record PhysicalInfo(
            Long id,
            Double height,
            Double weight,
            LocalDate birthDate,
            Gender gender
    ) {
        public static PhysicalInfo from(Ward ward) {
            return new PhysicalInfo(
                    ward.getId(),
                    ward.getHeight(),
                    ward.getWeight(),
                    ward.getBirthDate(),
                    ward.getGender()
            );
        }
    }
}
