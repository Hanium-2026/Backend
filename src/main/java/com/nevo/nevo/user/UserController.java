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

    /**
     * 사용자 기본 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo() {
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 기본 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo() {
        return ResponseEntity.ok().build();
    }

    /**
     * 기기 푸시 토큰(FCM TOKEN) 저장
     */
    @PostMapping("/device-token")
    public ResponseEntity<Void> updateDeviceToken(
            @Valid
            @RequestBody FcmTokenRequest fcmTokenRequest
    ) {
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 계정 탈퇴 (Soft Delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        return ResponseEntity.noContent().build();
    }
}
