package com.nevo.nevo.location.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.location.dto.response.LocationResponse;
import com.nevo.nevo.location.entity.LocationHistory;
import com.nevo.nevo.location.repository.LocationHistoryRepository;
import com.nevo.nevo.location.repository.LocationRepository;
import com.nevo.nevo.location.exception.code.LocationErrorCode;
import com.nevo.nevo.location.sse.SseEmitterManager;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardGuardianLinkRepository;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final WardRepository wardRepository;
    private final WardGuardianLinkRepository wardGuardianLinkRepository;
    private final SseEmitterManager sseEmitterManager;

    @Transactional
    public void updateLocation(Long wardId, Double latitude, Double longitude) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new CustomException(LocationErrorCode.WARD_NOT_FOUND));

        locationRepository.upsertLocation(wardId, latitude, longitude);
        locationHistoryRepository.save(LocationHistory.create(ward, latitude, longitude));

        sseEmitterManager.send(wardId, LocationResponse.Current.builder()
                .wardId(wardId)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(java.time.LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<LocationResponse.HistoryPoint> getHistory(Long guardianUserId, Long wardId, LocalDate date) {
        if (!wardGuardianLinkRepository.existsByWard_IdAndGuardian_Id(wardId, guardianUserId)) {
            throw new CustomException(WardErrorCode.WARD_LINK_NOT_FOUND);
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return locationHistoryRepository.findByWard_IdAndRecordedAtBetweenOrderByRecordedAtAsc(wardId, start, end)
                .stream()
                .map(history -> LocationResponse.HistoryPoint.builder()
                        .latitude(history.getLatitude())
                        .longitude(history.getLongitude())
                        .recordedAt(history.getRecordedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long wardId) {
        if (!wardRepository.existsById(wardId)) {
            throw new CustomException(LocationErrorCode.WARD_NOT_FOUND);
        }

        SseEmitter emitter = sseEmitterManager.subscribe(wardId);

        // 구독 즉시 DB의 마지막 위치 전송 (보호자 앱 진입 시 빈 화면 방지)
        locationRepository.findByWard_Id(wardId).ifPresent(location -> {
            try {
                emitter.send(SseEmitter.event().name("location")
                        .data(LocationResponse.Current.builder()
                                .wardId(wardId)
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude())
                                .recordedAt(location.getRecordedAt())
                                .build()));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}