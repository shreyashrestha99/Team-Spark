package com.Backend.Backend.dto.timeslot;

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
public class TimeSlotResponseDto {

    private UUID slotId;
    private DayOfWeek dayOfWeek;
    private Integer periodNumber;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Boolean isTeachingSlot;

    // Minutes this slot covers, used to size multi period sessions
    private long durationMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
