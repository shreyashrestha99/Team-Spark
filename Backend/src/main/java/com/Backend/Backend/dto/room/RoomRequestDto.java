package com.Backend.Backend.dto.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequestDto {

    @NotBlank(message = "Room code is required")
    @Size(max = 50, message = "Room code must be under 50 characters")
    private String roomCode;

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must be under 100 characters")
    private String roomName;

    // e.g. LECTURE, LAB, EXAM_HALL
    @Size(max = 50, message = "Room type must be under 50 characters")
    private String roomType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 1000, message = "Capacity must be 1000 or less")
    private Integer capacity;

    @Min(value = 0, message = "Floor cannot be negative")
    @Max(value = 100, message = "Floor must be 100 or less")
    private Integer floor;

    @Size(max = 100, message = "Building must be under 100 characters")
    private String building;

    // Facility flags default to false
    private Boolean hasProjector;
    private Boolean hasComputers;
    private Boolean hasAc;

    // Defaults to true when omitted
    private Boolean isAvailable;
}
