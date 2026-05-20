package com.nevo.nevo.user.repository;

import com.nevo.nevo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    // ID로 활성 사용자 조회
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
