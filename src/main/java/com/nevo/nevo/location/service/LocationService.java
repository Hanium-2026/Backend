package com.nevo.nevo.location.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.location.dto.response.LocationResponse;
import com.nevo.nevo.location.repository.LocationRepository;
import com.nevo.nevo.location.sse.SseEmitterManager;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final WardRepository wardRepository;
    private final SseEmitterManager sseEmitterManager;

    @Transactional
    public void updateLocation(Long wardId, Double latitude, Double longitude) {
        if (!wardRepository.existsById(wardId)) {
            throw new CustomException(WardErrorCode.WARD_NOT_FOUND);
        }

        locationRepository.upsertLocation(wardId, latitude, longitude);

        sseEmitterManager.send(wardId, LocationResponse.Current.builder()
                .wardId(wardId)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(java.time.LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long wardId) {
        if (!wardRepository.existsById(wardId)) {
            throw new CustomException(WardErrorCode.WARD_NOT_FOUND);
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