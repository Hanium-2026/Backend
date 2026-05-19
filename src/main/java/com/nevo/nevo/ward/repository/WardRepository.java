package com.nevo.nevo.ward.repository;

import com.nevo.nevo.ward.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByUser_Id(Long userId);

    // Ward + User를 한 번의 JOIN FETCH로 조회 (보호자 대시보드 N+1 방지)
    @Query("SELECT w FROM Ward w JOIN FETCH w.user WHERE w.id IN :wardIds")
    List<Ward> findAllByIdInWithUser(@Param("wardIds") List<Long> wardIds);
}
