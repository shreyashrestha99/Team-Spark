package com.Backend.Backend.dto.timetable;

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
public class TimetableSessionRequestDto {

    @NotNull(message = "Batch is required")
    private UUID batchId;

    @NotNull(message = "Module is required")
    private UUID moduleId;

    @NotNull(message = "Teacher is required")
    private UUID teacherId;

    @NotNull(message = "Room is required")
    private UUID roomId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    // e.g. LECTURE, TUTORIAL, LAB
    @Size(max = 50, message = "Session type must be under 50 characters")
    private String sessionType;

    // Defaults to SCHEDULED when omitted
    @Size(max = 30, message = "Status must be under 30 characters")
    private String status;
}
