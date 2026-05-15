package com.nevo.nevo.user.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.user.dto.UserRequest;
import com.nevo.nevo.user.dto.UserResponse;
import com.nevo.nevo.user.exception.code.UserSuccessCode;
import com.nevo.nevo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 기본 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponse.MyInfo>> getMyInfo(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        UserResponse.MyInfo data = userService.getMyInfo(auth.userId());
        return ResponseEntity.ok(
                SuccessResponse.of(UserSuccessCode.USER_FOUND, data)
        );
    }

    /**
     * 사용자 기본 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponse.MyInfo>> updateMyInfo(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody UserRequest.UpdateName updateRequestDto
    ) {
        UserResponse.MyInfo data = userService.updateMyInfo(auth.userId(), updateRequestDto);
        return ResponseEntity.ok(
                SuccessResponse.of(UserSuccessCode.USER_UPDATED, data)
        );
    }

    /**
     * 기기 푸시 토큰(FCM TOKEN) 저장
     */
    @PostMapping("/device-token")
    public ResponseEntity<SuccessResponse<Void>> updateDeviceToken(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody UserRequest.UpdateFcmToken fcmTokenRequest
    ) {
        userService.updateDeviceToken(auth.userId(), fcmTokenRequest);
        return ResponseEntity.ok(
                SuccessResponse.of(UserSuccessCode.FCM_TOKEN_UPDATED)
        );
    }

    /**
     * 기기 푸시 토큰(FCM TOKEN) 삭제 (로그아웃 시 클라이언트가 호출)
     */
    @DeleteMapping("/device-token")
    public ResponseEntity<SuccessResponse<Void>> deleteDeviceToken(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        userService.deleteDeviceToken(auth.userId());
        return ResponseEntity.ok(
                SuccessResponse.of(UserSuccessCode.FCM_TOKEN_DELETED)
        );
    }

    /**
     * 사용자 계정 탈퇴 (Soft Delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse<Void>> deleteAccount(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        userService.deleteAccount(auth.userId());
        return ResponseEntity.ok(
                SuccessResponse.of(UserSuccessCode.USER_DELETED)
        );
    }
}
