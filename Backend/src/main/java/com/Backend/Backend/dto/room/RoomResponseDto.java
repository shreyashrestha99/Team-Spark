package com.Backend.Backend.dto.room;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDto {

    private UUID roomId;
    private String roomCode;
    private String roomName;
    private String roomType;
    private Integer capacity;
    private Integer floor;
    private String building;

    private Boolean hasProjector;
    private Boolean hasComputers;
    private Boolean hasAc;
    private Boolean isAvailable;

    /** Human-friendly label for dropdowns, e.g. "LAB-201 — Computer Lab (40 seats)". */
    private String label;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
