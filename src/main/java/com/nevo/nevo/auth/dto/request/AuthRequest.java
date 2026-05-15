package com.nevo.nevo.auth.dto.request;

import com.nevo.nevo.user.entity.ConsentType;
import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.ward.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class AuthRequest {

    public record SignUp(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                    message = "비밀번호는 8자 이상, 영문·숫자·특수문자를 포함해야 합니다."
            )
            String password,

            @NotBlank(message = "이름은 필수입니다.")
            String name,

            @NotNull(message = "역할은 필수입니다.")
            Role role,

            @NotBlank(message = "디바이스 ID는 필수입니다.")
            String deviceId,

            // WARD 역할일 때만 입력 (GUARDIAN은 null 허용)
            Double height,
            Double weight,
            LocalDate birthDate,
            Gender gender,

            @NotEmpty(message = "약관 동의 항목은 필수입니다.")
            List<ConsentItem> consents
    ) {}

    public record Login(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            String password,

            @NotBlank(message = "디바이스 ID는 필수입니다.")
            String deviceId
    ) {}

    public record Logout(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {}

    public record Refresh(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {}

    public record PasswordResetRequest(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {}

    public record PasswordResetConfirm(
            @NotBlank(message = "토큰은 필수입니다.")
            String token,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                    message = "비밀번호는 8자 이상, 영문·숫자·특수문자를 포함해야 합니다."
            )
            String newPassword
    ) {}

    public record ConsentItem(
            @NotNull(message = "약관 종류는 필수입니다.")
            ConsentType consentType,

            @NotNull(message = "동의 여부는 필수입니다.")
            Boolean agreed
    ) {}
}
