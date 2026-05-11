package com.nevo.nevo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * 1. 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다. ID : " + userId));

        return UserResponseDto.from(user);
    }

    /**
     * 2. 사용자 정보 수정
     */
    @Transactional
    public UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다. ID : " + userId));

        user.updateName(requestDto.name());

        return UserResponseDto.from(user);
    }

    /**
     * 3. 기기 푸시 토큰(FCM Token) 저장
     */
    @Transactional
    public void updateDeviceToken(Long userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다. ID : " + userId));

        user.updateFcmToken(request.fcmToken());
    }

    /**
     * 4. 사용자 계정 탈퇴 (Soft delete)
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다. ID : " + userId));

        user.softDelete();
    }
}
