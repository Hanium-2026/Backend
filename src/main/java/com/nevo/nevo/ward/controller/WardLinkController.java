package com.nevo.nevo.ward.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.notification.dto.response.AlertResponse;
import com.nevo.nevo.notification.service.AlertService;
import com.nevo.nevo.ward.dto.request.WardLinkRequest;
import com.nevo.nevo.ward.dto.response.WardLinkResponse;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.exception.code.WardSuccessCode;
import com.nevo.nevo.ward.service.WardLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ward-link")
@RequiredArgsConstructor
public class WardLinkController {

    private final WardLinkService wardLinkService;
    private final AlertService alertService;

    @PostMapping("/code")
    public ResponseEntity<SuccessResponse<WardLinkResponse.CodeInfo>> generateCode(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody WardLinkRequest.GenerateCode request
    ) {
        if (auth.wardId() == null) throw new CustomException(WardErrorCode.WARD_ACCESS_DENIED);

        WardLinkResponse.CodeInfo data = wardLinkService.generateCode(auth.wardId(), request.guardianEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(WardSuccessCode.WARD_LINK_CODE_CREATED, data));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> connect(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody WardLinkRequest.Connect request
    ) {
        if (auth.wardId() != null) throw new CustomException(WardErrorCode.GUARDIAN_ACCESS_DENIED);

        wardLinkService.connect(auth.userId(), request.code());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(WardSuccessCode.WARD_LINK_CONNECTED));
    }

    @DeleteMapping("/{wardId}")
    public ResponseEntity<SuccessResponse<Void>> disconnect(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long wardId
    ) {
        if (auth.wardId() != null) throw new CustomException(WardErrorCode.GUARDIAN_ACCESS_DENIED);

        wardLinkService.disconnect(auth.userId(), wardId);
        return ResponseEntity.ok(SuccessResponse.of(WardSuccessCode.WARD_LINK_DISCONNECTED));
    }

    @GetMapping("/wards")
    public ResponseEntity<SuccessResponse<List<WardLinkResponse.WardInfo>>> getMyWards(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        if (auth.wardId() != null) throw new CustomException(WardErrorCode.GUARDIAN_ACCESS_DENIED);

        List<WardLinkResponse.WardInfo> data = wardLinkService.getMyWards(auth.userId());
        return ResponseEntity.ok(SuccessResponse.of(WardSuccessCode.WARD_LINK_WARDS_FOUND, data));
    }

    @GetMapping("/guardians")
    public ResponseEntity<SuccessResponse<List<WardLinkResponse.GuardianInfo>>> getMyGuardians(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        if (auth.wardId() == null) throw new CustomException(WardErrorCode.WARD_ACCESS_DENIED);

        List<WardLinkResponse.GuardianInfo> data = wardLinkService.getMyGuardians(auth.wardId());
        return ResponseEntity.ok(SuccessResponse.of(WardSuccessCode.WARD_LINK_GUARDIANS_FOUND, data));
    }

    @GetMapping("/{wardId}/alerts")
    public ResponseEntity<SuccessResponse<List<AlertResponse.AlertInfo>>> getAlerts(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long wardId
    ) {
        if (auth.wardId() != null) throw new CustomException(WardErrorCode.GUARDIAN_ACCESS_DENIED);

        List<AlertResponse.AlertInfo> data = alertService.getAlerts(auth.userId(), wardId);
        return ResponseEntity.ok(SuccessResponse.of(WardSuccessCode.WARD_ALERTS_FOUND, data));
    }
}