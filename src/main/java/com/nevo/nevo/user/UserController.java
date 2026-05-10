package com.nevo.nevo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 기본 정보 조회
     * GET /api/users/me
     */
    @GetMapping
    public ResponseEntity<?> getMyInfo() {
        return ResponseEntity.ok().body("내 정보 조회 성공");
    }

}
