package com.Backend.Backend.dto.seat;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAllocationResponseDto {

    private UUID allocationId;

    private UUID examId;
    private String moduleCode;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private UUID examRoomId;
    private UUID roomId;
    private String roomCode;
    private String roomName;

    private UUID studentId;
    private String studentNumber;
    private String studentName;

    private String seatNumber;
    private String rowNumber;
    private String columnNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
