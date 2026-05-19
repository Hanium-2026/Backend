package com.nevo.nevo.ward.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class WardLinkRequest {

    public record GenerateCode(
            @NotBlank(message = "보호자 이메일은 필수입니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String guardianEmail
    ) {}

    public record Connect(
            @NotBlank(message = "연동 코드는 필수입니다.")
            String code
    ) {}
}