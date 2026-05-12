package com.nevo.nevo.user.repository;

import com.nevo.nevo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 이메일 중복 가입 여부 확인
    boolean existsByEmail(String email);
}
