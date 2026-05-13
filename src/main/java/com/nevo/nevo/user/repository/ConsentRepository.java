package com.nevo.nevo.user.repository;

import com.nevo.nevo.user.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
}
