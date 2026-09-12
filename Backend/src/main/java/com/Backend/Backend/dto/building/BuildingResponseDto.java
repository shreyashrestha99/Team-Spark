package com.Backend.Backend.dto.building;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingResponseDto {

    private UUID buildingId;
    private String buildingName;
    private String buildingCode;
    private Integer floors;
    private Boolean isActive;

    // How many rooms sit in this building
    private int roomCount;

    // Total teaching seats across those rooms
    private int totalCapacity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
