package com.nevo.nevo.auth.repository;

import com.nevo.nevo.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //토큰 해시로 조회했을 때 해당 토큰이 존재하지 않을 수 있기 때문에 Optional.
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser_Id(Long userId);

    List<RefreshToken> findAllByUser_IdAndDeviceIdAndRevokedFalse(Long userId, String deviceId);

    @Query("""
            select token from RefreshToken token
                where token.user.id = :userId
                    and token.revoked = false and token.used = false
                        order by token.id asc
    """)
    List<RefreshToken> findAllActiveRefreshToken(@Param("userId") Long userId);

    Optional<RefreshToken> findByTokenHashAndRevokedFalseAndUsedFalse(String tokenHash);
}
