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

    @PostMapping("/sign-up")
    @Operation(summary = "회원가입", description = "WARD 또는 GUARDIAN 역할로 회원가입합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse.SignUp>> signUp(
            @RequestBody @Valid AuthRequest.SignUp request)
    {
        AuthResponse.SignUp response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(AuthSuccessCode.SIGN_UP_SUCCESS, response));
    }
}
