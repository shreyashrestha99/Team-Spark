package com.Backend.Backend.dto.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAvailabilityRequestDto {

    @NotNull(message = "Lecturer is required")
    private UUID teacherId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    // UNAVAILABLE blocks the slot, PREFERRED only nudges the score. Defaults to UNAVAILABLE.
    private String availabilityType;

    @Size(max = 255, message = "Note must be under 255 characters")
    private String note;
}
