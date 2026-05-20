package com.nevo.nevo.auth.dto.request;

import com.nevo.nevo.auth.entity.SmsVerificationPurpose;
import com.nevo.nevo.user.entity.ConsentType;
import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.ward.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class AuthRequest {

    public record SignUp(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,

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
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,

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
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone
    ) {}

    public record PasswordResetConfirm(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                    message = "비밀번호는 8자 이상, 영문·숫자·특수문자를 포함해야 합니다."
            )
            String newPassword
    ) {}

    // SMS OTP 발송 요청
    public record SmsSend(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,

            @NotNull(message = "목적은 필수입니다.")
            SmsVerificationPurpose purpose
    ) {}

    // SMS OTP 인증 요청
    public record SmsVerify(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,

            @NotBlank(message = "인증번호는 필수입니다.")
            @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
            String code,

            @NotNull(message = "목적은 필수입니다.")
            SmsVerificationPurpose purpose
    ) {}

    public record ConsentItem(
            @NotNull(message = "약관 종류는 필수입니다.")
            ConsentType consentType,

            @NotNull(message = "동의 여부는 필수입니다.")
            Boolean agreed
    ) {}
}
