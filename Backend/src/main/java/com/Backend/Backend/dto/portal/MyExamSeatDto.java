package com.Backend.Backend.dto.portal;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** One exam a student sits, and exactly where they sit it. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyExamSeatDto {

    private UUID examId;
    private String moduleCode;
    private String moduleName;
    private String examType;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;

    // Where to go
    private String buildingName;
    private String roomCode;
    private String roomName;

    // Where to sit
    private String seatNumber;
    private String rowLabel;
    private String columnNumber;

    private String status;

    /** False once the exam date has passed, so the UI can grey it out. */
    private boolean upcoming;

    // Days until the exam, negative once it is behind them
    private long daysAway;
}
