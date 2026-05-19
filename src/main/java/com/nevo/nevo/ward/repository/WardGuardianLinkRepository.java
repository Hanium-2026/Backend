package com.nevo.nevo.ward.repository;

import com.nevo.nevo.ward.entity.WardGuardianLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WardGuardianLinkRepository extends JpaRepository<WardGuardianLink, Long> {

    List<WardGuardianLink> findAllByGuardian_Id(Long guardianUserId);

    List<WardGuardianLink> findAllByWard_Id(Long wardId);

    Optional<WardGuardianLink> findByWard_IdAndGuardian_Id(Long wardId, Long guardianUserId);

    boolean existsByWard_IdAndGuardian_Id(Long wardId, Long guardianUserId);

    void deleteByWard_IdAndGuardian_Id(Long wardId, Long guardianUserId);
}
