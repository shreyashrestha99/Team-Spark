package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "building_id", updatable = false, nullable = false)
    private UUID buildingId;

    @Column(name = "building_name", nullable = false, unique = true, length = 100)
    private String buildingName;

    @Column(name = "building_code", nullable = false, unique = true, length = 20)
    private String buildingCode;

    @Column(name = "floors")
    private Integer floors;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "building")
    private List<RoomEntity> rooms = new ArrayList<>();
}
