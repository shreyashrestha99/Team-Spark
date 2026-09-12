package com.Backend.Backend.dto.timetable;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleConflictDto {

    /** ROOM_DOUBLE_BOOKED, TEACHER_BUSY, BATCH_OVERLAP or CAPACITY_EXCEEDED. */
    private String type;

    // Human-readable explanation
    private String message;

    // The session already holding the slot
    private UUID conflictingSessionId;
}
