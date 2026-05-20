package com.nevo.nevo.session.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class SessionRequest {

    // 세션 종합 분석 결과 업로드 요청
    // 앱이 세션 종료 후 TFLite 분석을 완료하면 전송
    public record AnalysisUpload(
            // NORMAL: 정상 / SUSPECTED: 뇌졸중 의심
            @NotBlank @Pattern(regexp = "NORMAL|SUSPECTED", message = "riskLevel은 NORMAL 또는 SUSPECTED여야 합니다.") String riskLevel,
            @NotNull Float avgScore,
            @NotNull Float minScore,
            @NotNull Float maxScore,
            // 0 초과 시 StrokeDangerEvent 발행 → FCM 알림
            @NotNull @Min(0) Integer dangerCount,
            String reportSummary,
            Float variabilityScore,   // AI팀 확정 전까지 nullable — 확정 시 @NotNull 추가
            Float asymmetryScore      // AI팀 확정 전까지 nullable — 확정 시 @NotNull 추가
    ) {}

    // 분당 보행 데이터 배치 업로드 요청
    // 오프라인 중 SQLite에 쌓인 데이터를 네트워크 복구 시 한 번에 전송
    public record DataUpload(
            @NotNull @Size(min = 1) @Valid List<MinuteData> data
    ) {}

    // 앱 슬라이딩 윈도우(2초) 분석 → 분당 집계 결과 1건
    public record MinuteData(
            @NotNull LocalDateTime minuteAt,
            @NotNull Float avgScore,
            @NotNull Float minScore,
            @NotNull Float maxScore,
            @NotNull @Min(0) Integer dangerCount
    ) {}
}
