package com.nevo.nevo.ward.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class WardLinkRequest {

    public record GenerateCode(
            @NotBlank(message = "보호자 전화번호는 필수입니다.")
            @Pattern(
                    regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
                    message = "올바른 전화번호 형식이 아닙니다."
            )
            String guardianPhone
    ) {}

    public record Connect(
            @NotBlank(message = "연동 코드는 필수입니다.")
            String code
    ) {}
}