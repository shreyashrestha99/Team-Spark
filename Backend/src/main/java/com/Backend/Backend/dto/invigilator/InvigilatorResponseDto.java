package com.Backend.Backend.dto.invigilator;

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
public class InvigilatorResponseDto {

    private UUID invigilatorId;

    private UUID examId;
    private String moduleCode;
    private String batchName;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private UUID userId;
    private String fullName;
    private String email;
    private String userRole;

    // Duty role for this exam
    private String role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
