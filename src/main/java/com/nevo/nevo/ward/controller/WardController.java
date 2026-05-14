package com.nevo.nevo.ward.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.ward.dto.WardRequest;
import com.nevo.nevo.ward.dto.WardResponse;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.exception.code.WardSuccessCode;
import com.nevo.nevo.ward.service.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;
    /**
     * 노약자 신체 정보 조회
     */
    @GetMapping("/me/physical-info")
    public ResponseEntity<SuccessResponse<WardResponse.PhysicalInfo>> getMyPhysicalInfo(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        if (auth.wardId() == null) throw new CustomException(WardErrorCode.WARD_ACCESS_DENIED);
        WardResponse.PhysicalInfo data = wardService.getMyPhysicalInfo(auth.wardId());

        return ResponseEntity.ok(
                SuccessResponse.of(WardSuccessCode.WARD_PHYSICAL_INFO_FOUND, data)
        );
    }

    /**
     * 노약자 신체 정보 등록 및 수정
     */
    @PutMapping("/me/physical-info")
    public ResponseEntity<SuccessResponse<WardResponse.PhysicalInfo>> updateMyPhysicalInfo(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody WardRequest.UpsertPhysicalInfo request
    ) {
        if (auth.wardId() == null) throw new CustomException(WardErrorCode.WARD_ACCESS_DENIED);
        WardResponse.PhysicalInfo data = wardService.updateMyPhysicalInfo(auth.wardId(), request);

        return ResponseEntity.ok(
                SuccessResponse.of(WardSuccessCode.WARD_PHYSICAL_INFO_UPDATED, data)
        );
    }

}
