package com.Backend.Backend.dto.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAvailabilityResponseDto {

    private UUID availabilityId;
    private UUID teacherId;
    private String teacherName;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String availabilityType;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
