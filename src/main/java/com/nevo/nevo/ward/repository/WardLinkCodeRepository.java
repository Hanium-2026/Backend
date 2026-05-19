package com.nevo.nevo.ward.repository;

import com.nevo.nevo.ward.entity.WardLinkCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WardLinkCodeRepository extends JpaRepository<WardLinkCode, Long> {

    Optional<WardLinkCode> findByCodeAndUsedFalse(String code);

    boolean existsByCode(String code);

    // 새 코드 발급 시 기존 미사용 코드 일괄 삭제
    @Modifying
    void deleteByWard_IdAndUsedFalse(Long wardId);

    // 만료 코드 정기 정리용
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime now);
}
