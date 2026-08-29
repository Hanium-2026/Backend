package com.nevo.nevo.location.entity;

import com.nevo.nevo.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "latitude", nullable = false, columnDefinition = "NUMERIC(10, 8)")
    private Double latitude;

    @Column(name = "longitude", nullable = false, columnDefinition = "NUMERIC(11, 8)")
    private Double longitude;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public static LocationHistory create(Ward ward, Double latitude, Double longitude) {
        LocationHistory history = new LocationHistory();
        history.ward = ward;
        history.latitude = latitude;
        history.longitude = longitude;
        history.recordedAt = LocalDateTime.now();
        return history;
    }
}