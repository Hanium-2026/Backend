package com.nevo.nevo.ward.repository;

import com.nevo.nevo.ward.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByUser_Id(Long userId);
}
