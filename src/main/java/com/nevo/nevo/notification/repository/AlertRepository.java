package com.nevo.nevo.notification.repository;

import com.nevo.nevo.notification.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByWard_IdOrderByCreatedAtDesc(Long wardId);
}