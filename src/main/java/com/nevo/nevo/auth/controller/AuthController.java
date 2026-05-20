package com.nevo.nevo.auth.controller;

import com.nevo.nevo.auth.dto.request.AuthRequest;
import com.nevo.nevo.auth.dto.response.AuthResponse;
import com.nevo.nevo.auth.exception.code.AuthSuccessCode;
import com.nevo.nevo.auth.service.AuthService;
import com.nevo.nevo.global.exception.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    @Operation(summary = "SMS OTP 발송", description = "회원가입 또는 비밀번호 재설정을 위한 SMS 인증번호를 발송합니다.")
    public ResponseEntity<SuccessResponse<Void>> sendSms(
            @RequestBody @Valid AuthRequest.SmsSend request)
    {
        authService.sendSms(request.phone(), request.purpose());
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.OTP_SENT, null));
    }

    @PostMapping("/sms/verify")
    @Operation(summary = "SMS OTP 인증", description = "발송된 인증번호를 검증합니다. 성공 시 10분간 인증 상태가 유지됩니다.")
    public ResponseEntity<SuccessResponse<Void>> verifySms(
            @RequestBody @Valid AuthRequest.SmsVerify request)
    {
        authService.verifySms(request.phone(), request.code(), request.purpose());
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.OTP_VERIFIED, null));
    }

    @PostMapping("/sign-up")
    @Operation(summary = "회원가입", description = "SMS 인증 완료 후 WARD 또는 GUARDIAN 역할로 회원가입합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse.SignUp>> signUp(
            @RequestBody @Valid AuthRequest.SignUp request)
    {
        AuthResponse.SignUp response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(AuthSuccessCode.SIGN_UP_SUCCESS, response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "전화번호/비밀번호로 로그인합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse.Login>> login(
            @RequestBody @Valid AuthRequest.Login request)
    {
        AuthResponse.Login response = authService.login(request);
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.LOGIN_SUCCESS, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화합니다.")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestBody @Valid AuthRequest.Logout request)
    {
        authService.logout(request);
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스/리프레시 토큰을 발급합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse.Refresh>> refresh(
            @RequestBody @Valid AuthRequest.Refresh request)
    {
        AuthResponse.Refresh response = authService.refresh(request);
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.TOKEN_REFRESH_SUCCESS, response));
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "비밀번호 재설정 요청", description = "전화번호로 비밀번호 재설정 SMS 인증번호를 발송합니다.")
    public ResponseEntity<SuccessResponse<Void>> requestPasswordReset(
            @RequestBody @Valid AuthRequest.PasswordResetRequest request)
    {
        authService.requestPasswordReset(request.phone());
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.OTP_SENT, null));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "비밀번호 재설정 확인", description = "SMS 인증 완료 후 새 비밀번호로 변경합니다.")
    public ResponseEntity<SuccessResponse<Void>> confirmPasswordReset(
            @RequestBody @Valid AuthRequest.PasswordResetConfirm request)
    {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.PASSWORD_RESET_SUCCESS, null));
    }
}
