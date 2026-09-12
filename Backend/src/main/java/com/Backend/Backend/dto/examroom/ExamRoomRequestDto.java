package com.Backend.Backend.dto.examroom;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomRequestDto {

    @NotNull(message = "Exam is required")
    private UUID examId;

    @NotNull(message = "Room is required")
    private UUID roomId;

    @NotNull(message = "Allocated capacity is required")
    @Min(value = 1, message = "Allocated capacity must be at least 1")
    private Integer allocatedCapacity;

    @Min(value = 0, message = "Seat count cannot be negative")
    private Integer seatCount;

    // Defaults to ALLOCATED when omitted
    @Size(max = 30, message = "Status must be under 30 characters")
    private String status;
}
