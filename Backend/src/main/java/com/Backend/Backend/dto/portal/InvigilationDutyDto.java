package com.Backend.Backend.dto.portal;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** One exam a lecturer has been rostered to invigilate. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilationDutyDto {

    private UUID invigilatorId;
    private UUID examId;

    private String moduleCode;
    private String moduleName;
    private String batchName;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;

    // CHIEF or INVIGILATOR
    private String role;

    /** Halls to cover, e.g. "Himal Block · HML-301". */
    private List<String> rooms;

    private int candidateCount;

    private boolean upcoming;
    private long daysAway;
}
