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

    private UUID buildingId;
    private String buildingName;
    private String buildingCode;

    // Seat grid. Rows are derived, never typed by the admin.
    private Integer seatRows;
    private Integer seatsPerRow;
    private Double examCapacityFactor;

    /** Seats usable once exam spacing is applied, the number venue allocation actually uses. */
    private Integer examCapacity;

    private Boolean hasProjector;
    private Boolean hasComputers;
    private Boolean hasAc;
    private Boolean isAvailable;

    /** Human-friendly label for dropdowns, e.g. "LAB-201 — Computer Lab (40 seats)". */
    private String label;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
