package com.nevo.nevo.notification.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.notification.dto.response.AlertResponse;
import com.nevo.nevo.notification.entity.Alert;
import com.nevo.nevo.notification.entity.AlertType;
import com.nevo.nevo.notification.repository.AlertRepository;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardGuardianLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final WardGuardianLinkRepository wardGuardianLinkRepository;

    @Transactional
    public void save(Ward ward, AlertType type, Long sessionId, String message) {
        alertRepository.save(Alert.builder()
                .ward(ward)
                .type(type)
                .sessionId(sessionId)
                .message(message)
                .build());
    }

    public List<AlertResponse.AlertInfo> getAlerts(Long guardianUserId, Long wardId) {
        if (!wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, guardianUserId)) {
            throw new CustomException(WardErrorCode.WARD_LINK_NOT_FOUND);
        }
        return alertRepository.findAllByWard_IdOrderByCreatedAtDesc(wardId)
                .stream()
                .map(AlertResponse.AlertInfo::from)
                .toList();
    }
}