package com.nevo.nevo.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // JWT 인증 로직이 완성되기 전까지 사용할 임시 ID
    private final Long TEMP_USER_ID = 1L;

    /**
     * 사용자 기본 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo() {
        UserResponseDto response = userService.getMyInfo(TEMP_USER_ID);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 기본 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @Valid
            @RequestBody UserUpdateRequestDto updateRequestDto
    ) {
        UserResponseDto response = userService.updateMyInfo(TEMP_USER_ID, updateRequestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 기기 푸시 토큰(FCM TOKEN) 저장
     */
    @PostMapping("/device-token")
    public ResponseEntity<Void> updateDeviceToken(
            @Valid
            @RequestBody FcmTokenRequest fcmTokenRequest
    ) {
        userService.updateDeviceToken(TEMP_USER_ID, fcmTokenRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 계정 탈퇴 (Soft Delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount(TEMP_USER_ID);
        return ResponseEntity.noContent().build();
    }
}
