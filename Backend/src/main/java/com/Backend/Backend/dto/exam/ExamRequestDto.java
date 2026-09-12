package com.Backend.Backend.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRequestDto {

    @NotNull(message = "Module is required")
    private UUID moduleId;

    @NotNull(message = "Batch is required")
    private UUID batchId;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    // e.g. FINAL, MIDTERM, RESIT
    @Size(max = 50, message = "Exam type must be under 50 characters")
    private String examType;

    /**
     * Allocates halls and seats the moment the exam is created. Defaults to true,
     * so scheduling an exam produces a finished seating plan in one step.
     */
    private Boolean autoAllocateSeating;

    // Defaults to SCHEDULED when omitted
    @Size(max = 30, message = "Status must be under 30 characters")
    private String status;
}
