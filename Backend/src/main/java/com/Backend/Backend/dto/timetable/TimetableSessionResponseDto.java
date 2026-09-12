package com.Backend.Backend.dto.timetable;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSessionResponseDto {

    private UUID sessionId;

    private UUID batchId;
    private String batchName;

    private UUID moduleId;
    private String moduleCode;
    private String moduleName;

    private UUID teacherId;
    private String teacherName;

    private UUID roomId;
    private String roomCode;
    private String roomName;
    private Integer roomCapacity;

    private UUID groupId;
    private String groupName;

    private UUID slotId;
    private Integer periodNumber;
    private DayOfWeek dayOfWeek;

    private UUID runId;

    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionType;
    private String status;

    // Length of the session in minutes
    private long durationMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
