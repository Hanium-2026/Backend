package com.nevo.nevo.session.controller;

import com.nevo.nevo.auth.jwt.JwtAuthentication;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.SuccessResponse;
import com.nevo.nevo.session.dto.request.SessionRequest;
import com.nevo.nevo.session.dto.response.SessionResponse;
import com.nevo.nevo.session.exception.code.SessionErrorCode;
import com.nevo.nevo.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gait/sessions")
@RequiredArgsConstructor
@Tag(name = "Session", description = "보행 세션 API")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    @Operation(summary = "보행 측정 시작", description = "보행 측정 세션을 시작합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<SessionResponse.Start>> start(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        if (auth.wardId() == null) throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        return sessionService.start(auth.wardId());
    }

    @GetMapping("/active")
    @Operation(summary = "진행 중인 세션 조회", description = "현재 진행 중인 보행 세션을 조회합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<SessionResponse.Active>> getActive(
            @AuthenticationPrincipal JwtAuthentication auth
    ) {
        if (auth.wardId() == null) throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        return sessionService.getActive(auth.wardId());
    }

    @PostMapping("/{sessionId}/stop")
    @Operation(summary = "보행 측정 종료", description = "보행 측정 세션을 종료합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<Void>> stop(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long sessionId
    ) {
        if (auth.wardId() == null) throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        return sessionService.stop(sessionId, auth.wardId());
    }

    @PostMapping("/{sessionId}/data")
    @Operation(summary = "보행 데이터 전송", description = "분당 집계된 보행 데이터를 배치로 전송합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<SessionResponse.DataUpload>> uploadData(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionRequest.DataUpload request
    ) {
        if (auth.wardId() == null) throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        return sessionService.uploadData(sessionId, auth.wardId(), request);
    }

    @PostMapping("/{sessionId}/analysis")
    @Operation(summary = "분석 결과 업로드", description = "보행 분석 결과를 업로드합니다. (WARD 전용)")
    public ResponseEntity<SuccessResponse<Void>> uploadAnalysis(
            @AuthenticationPrincipal JwtAuthentication auth,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionRequest.AnalysisUpload request
    ) {
        if (auth.wardId() == null) throw new CustomException(SessionErrorCode.SESSION_FORBIDDEN);
        return sessionService.uploadAnalysis(sessionId, auth.wardId(), request);
    }
}
