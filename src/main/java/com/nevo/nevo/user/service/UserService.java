package com.nevo.nevo.user.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.user.dto.UserRequest;
import com.nevo.nevo.user.dto.UserResponse;
import com.nevo.nevo.user.entity.User;
import com.nevo.nevo.user.exception.code.UserErrorCode;
import com.nevo.nevo.user.repository.UserRepository;
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
    public UserResponse.MyInfo getMyInfo(Long userId) {
        User user = findActiveUser(userId);

        return UserResponse.MyInfo.from(user);
    }

    /**
     * 2. 사용자 정보 수정
     */
    @Transactional
    public UserResponse.MyInfo updateMyInfo(Long userId, UserRequest.UpdateName requestDto) {
        User user = findActiveUser(userId);

        user.updateName(requestDto.name());

        return UserResponse.MyInfo.from(user);
    }

    /**
     * 3. 기기 푸시 토큰(FCM Token) 저장
     */
    @Transactional
    public void updateDeviceToken(Long userId, UserRequest.UpdateFcmToken request) {
        User user = findActiveUser(userId);

        user.updateFcmToken(request.fcmToken());
    }

    /**
     * 4. FCM 토큰 삭제 (로그아웃 시 클라이언트가 별도 호출)
     */
    @Transactional
    public void deleteDeviceToken(Long userId) {
        User user = findActiveUser(userId);
        user.clearFcmToken();
    }

    /**
     * 5. 사용자 계정 탈퇴 (Soft delete)
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = findActiveUser(userId);

        user.softDelete();
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }
}
