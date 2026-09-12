package com.Backend.Backend.dto.exam;

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
public class ExamResponseDto {

    private UUID examId;

    private UUID moduleId;
    private String moduleCode;
    private String moduleName;

    private UUID batchId;
    private String batchName;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String examType;
    private Integer totalStudents;
    private String status;

    // Venue and staffing progress
    private int roomCount;
    private int allocatedCapacity;
    private int invigilatorCount;
    private int seatsAllocated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
