package com.nevo.nevo.ward.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.notification.service.FcmService;
import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.user.entity.User;
import com.nevo.nevo.user.exception.code.UserErrorCode;
import com.nevo.nevo.user.repository.UserRepository;
import com.nevo.nevo.ward.dto.response.WardLinkResponse;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.entity.WardGuardianLink;
import com.nevo.nevo.ward.entity.WardLinkCode;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardGuardianLinkRepository;
import com.nevo.nevo.ward.repository.WardLinkCodeRepository;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WardLinkService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final WardGuardianLinkRepository wardGuardianLinkRepository;
    private final WardLinkCodeRepository wardLinkCodeRepository;
    private final FcmService fcmService;

    @Transactional
    public WardLinkResponse.CodeInfo generateCode(Long wardId, String guardianEmail) {
        Ward ward = findWard(wardId);

        User guardian = userRepository.findByEmailAndDeletedAtIsNull(guardianEmail)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (guardian.getRole() != Role.GUARDIAN) {
            throw new CustomException(WardErrorCode.WARD_LINK_INVALID_GUARDIAN);
        }

        wardLinkCodeRepository.deleteByWard_IdAndUsedFalse(wardId);

        String code = generateUniqueCode();
        WardLinkCode linkCode = WardLinkCode.builder()
                .ward(ward)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();

        WardLinkCode saved = wardLinkCodeRepository.save(linkCode);

        if (guardian.getFcmToken() != null) {
            String wardName = ward.getUser().getName();
            fcmService.send(
                    guardian.getFcmToken(),
                    "연동 요청",
                    wardName + "님이 연동을 요청했습니다.",
                    Map.of("type", "LINK_REQUEST", "code", code)
            );
        }

        return WardLinkResponse.CodeInfo.from(saved);
    }

    @Transactional
    public void connect(Long guardianUserId, String code) {
        WardLinkCode linkCode = wardLinkCodeRepository.findByCodeAndUsedFalse(code)
                .orElseThrow(() -> new CustomException(WardErrorCode.WARD_LINK_CODE_NOT_FOUND));

        if (linkCode.isExpired()) {
            throw new CustomException(WardErrorCode.WARD_LINK_CODE_EXPIRED);
        }

        Long wardId = linkCode.getWard().getId();
        if (wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, guardianUserId)) {
            throw new CustomException(WardErrorCode.WARD_LINK_ALREADY_EXISTS);
        }

        User guardian = findUser(guardianUserId);
        WardGuardianLink link = WardGuardianLink.builder()
                .ward(linkCode.getWard())
                .guardian(guardian)
                .build();

        wardGuardianLinkRepository.save(link);
        linkCode.markUsed();
    }

    @Transactional
    public void disconnect(Long guardianUserId, Long wardId) {
        if (!wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, guardianUserId)) {
            throw new CustomException(WardErrorCode.WARD_LINK_NOT_FOUND);
        }
        wardGuardianLinkRepository.deleteByWard_IdAndGuardian_Id(wardId, guardianUserId);
    }

    public List<WardLinkResponse.WardInfo> getMyWards(Long guardianUserId) {
        return wardGuardianLinkRepository.findAllByGuardian_Id(guardianUserId)
                .stream()
                .map(WardLinkResponse.WardInfo::from)
                .toList();
    }

    public List<WardLinkResponse.GuardianInfo> getMyGuardians(Long wardId) {
        return wardGuardianLinkRepository.findAllByWard_Id(wardId)
                .stream()
                .map(WardLinkResponse.GuardianInfo::from)
                .toList();
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (wardLinkCodeRepository.existsByCode(code));
        return code;
    }

    private Ward findWard(Long wardId) {
        return wardRepository.findById(wardId)
                .orElseThrow(() -> new CustomException(WardErrorCode.WARD_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(WardErrorCode.WARD_NOT_FOUND));
    }
}