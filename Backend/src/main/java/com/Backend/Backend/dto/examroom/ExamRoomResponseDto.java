package com.Backend.Backend.dto.examroom;

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
public class ExamRoomResponseDto {

    private UUID examRoomId;

    private UUID examId;
    private String moduleCode;
    private String batchName;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private UUID roomId;
    private String roomCode;
    private String roomName;
    private Integer roomCapacity;

    private Integer allocatedCapacity;
    private Integer seatCount;
    private String status;

    // Seats already assigned in this room
    private int seatsAllocated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
