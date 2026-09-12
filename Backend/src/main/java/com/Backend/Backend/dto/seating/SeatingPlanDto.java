package com.Backend.Backend.dto.seating;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** The full seating plan for one exam: which halls, which desk, which candidate. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingPlanDto {

    private UUID examId;
    private String moduleCode;
    private String moduleName;
    private String batchName;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private int totalStudents;
    private int seatedStudents;

    // Candidates with nowhere to sit, the number that must reach zero
    private int unseatedStudents;

    private List<SeatingRoomDto> rooms;

    /** Plain-language notes, such as which block ran out of seats and what to do. */
    private List<String> warnings;

    private long allocationMillis;
}
