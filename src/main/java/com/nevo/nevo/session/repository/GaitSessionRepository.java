package com.nevo.nevo.session.repository;

import com.nevo.nevo.session.entity.GaitSession;
import com.nevo.nevo.session.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GaitSessionRepository extends JpaRepository<GaitSession, Long> {

    // 특정 피보호자의 진행 중 세션 조회 (세션 시작 중복 방지, 앱 재시작 시 세션 복원용)
    Optional<GaitSession> findByWard_IdAndStatus(Long wardId, SessionStatus status);

    // 자정 스케줄러에서 모든 ACTIVE 세션 자동 종료 처리용
    List<GaitSession> findByStatus(SessionStatus status);
}
