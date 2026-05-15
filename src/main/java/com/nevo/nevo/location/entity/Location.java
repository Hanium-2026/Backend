package com.nevo.nevo.location.entity;

import com.nevo.nevo.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
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

    public static Location create(Ward ward, Double latitude, Double longitude) {
        Location location = new Location();
        location.ward = ward;
        location.latitude = latitude;
        location.longitude = longitude;
        location.recordedAt = LocalDateTime.now();
        return location;
    }
}
