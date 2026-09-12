package com.Backend.Backend.dto.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAllocationRequestDto {

    @NotNull(message = "Exam is required")
    private UUID examId;

    @NotNull(message = "Exam room is required")
    private UUID examRoomId;

    @NotNull(message = "Student is required")
    private UUID studentId;

    @NotBlank(message = "Seat number is required")
    @Size(max = 30, message = "Seat number must be under 30 characters")
    private String seatNumber;

    @Size(max = 20, message = "Row must be under 20 characters")
    private String rowNumber;

    @Size(max = 20, message = "Column must be under 20 characters")
    private String columnNumber;
}
