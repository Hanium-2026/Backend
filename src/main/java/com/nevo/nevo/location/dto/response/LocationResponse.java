package com.nevo.nevo.location.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class LocationResponse {

    @Builder
    public record Current(
            Long wardId,
            Double latitude,
            Double longitude,
            LocalDateTime recordedAt
    ) {}

    @Builder
    public record HistoryPoint(
            Double latitude,
            Double longitude,
            LocalDateTime recordedAt
    ) {}
}