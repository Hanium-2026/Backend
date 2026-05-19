package com.nevo.nevo.ward.dto.response;

import com.nevo.nevo.ward.entity.WardGuardianLink;
import com.nevo.nevo.ward.entity.WardLinkCode;

import java.time.LocalDateTime;

public class WardLinkResponse {

    public record CodeInfo(
            String code,
            LocalDateTime expiresAt
    ) {
        public static CodeInfo from(WardLinkCode wardLinkCode) {
            return new CodeInfo(wardLinkCode.getCode(), wardLinkCode.getExpiresAt());
        }
    }

    public record WardInfo(
            Long wardId,
            String name,
            LocalDateTime linkedAt
    ) {
        public static WardInfo from(WardGuardianLink link) {
            return new WardInfo(
                    link.getWard().getId(),
                    link.getWard().getUser().getName(),
                    link.getCreatedAt()
            );
        }
    }

    public record GuardianInfo(
            Long guardianUserId,
            String name,
            LocalDateTime linkedAt
    ) {
        public static GuardianInfo from(WardGuardianLink link) {
            return new GuardianInfo(
                    link.getGuardian().getId(),
                    link.getGuardian().getName(),
                    link.getCreatedAt()
            );
        }
    }
}