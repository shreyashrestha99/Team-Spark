package com.Backend.Backend.scheduling;

import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What one run of the generator worked out, before any of it is written to the database. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationOutcome {

    @Builder.Default
    private List<PlannedSession> plan = new ArrayList<>();

    // Requirements that found no legal home, each with the reason it could not
    @Builder.Default
    private Map<SessionRequirement, String> unplaced = new LinkedHashMap<>();

    private int penaltyBeforeOptimise;
    private int penaltyAfterOptimise;
    private int idleGapsBefore;
    private int idleGapsAfter;

    // How many times the search had to undo a placement, useful when tuning
    private int backtracks;

    public boolean isComplete() {
        return unplaced.isEmpty();
    }
}
