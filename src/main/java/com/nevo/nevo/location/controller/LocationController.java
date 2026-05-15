package com.nevo.nevo.location.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.location.dto.request.LocationRequest;
import com.nevo.nevo.location.exception.code.LocationErrorCode;
import com.nevo.nevo.location.exception.code.LocationSuccessCode;
import com.nevo.nevo.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location", description = "위치 API")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @Operation(summary = "위치 업로드", description = "노약자가 현재 위치를 전송합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<Void>> uploadLocation(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody LocationRequest.Upload request
    ) {
        if (auth.wardId() == null) throw new CustomException(LocationErrorCode.WARD_ONLY);
        locationService.updateLocation(auth.wardId(), request.latitude(), request.longitude());
        return ResponseEntity.ok(SuccessResponse.of(LocationSuccessCode.LOCATION_UPDATED));
    }

    @GetMapping(value = "/stream/{wardId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "위치 스트림 구독", description = "보호자가 노약자의 실시간 위치를 구독합니다. (GUARDIAN 전용)")
    public SseEmitter subscribe(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long wardId
    ) {
        if (auth.wardId() != null) throw new CustomException(LocationErrorCode.GUARDIAN_ONLY);
        return locationService.subscribe(wardId);
    }
}