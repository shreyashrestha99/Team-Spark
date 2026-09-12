package com.Backend.Backend.dto.generation;

import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import lombok.*;

import java.util.List;

/**
 * What one generator run produced: the saved run, the weekly pattern it settled on,
 * anything it could not place, and the numbers that show the optimiser did something.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationResultDto {

    private GenerationRunResponseDto run;

    // One week of the routine, the pattern that was stamped across the whole window
    private List<TimetableSessionResponseDto> weeklyPattern;

    private List<UnplacedRequirementDto> unplaced;

    // Penalty before and after the local search pass, the proof it improved the plan
    private Integer penaltyBeforeOptimise;
    private Integer penaltyAfterOptimise;

    // Idle hours a student sits through in one week, before and after
    private Integer idleGapsBefore;
    private Integer idleGapsAfter;

    private long generationMillis;
}
