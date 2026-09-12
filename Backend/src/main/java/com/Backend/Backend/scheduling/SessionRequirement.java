package com.Backend.Backend.scheduling;

import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.entity.TeacherEntity;
import lombok.*;

/**
 * One teaching event the generator has to find a home for, e.g.
 * "Software Engineering workshop for Group B, 120 minutes, 30 students, needs a lab".
 *
 * These are expanded from the delivery pattern on BatchModule. They are what the
 * generator actually schedules, rather than the modules themselves.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRequirement {

    private BatchEntity batch;
    private ModuleEntity module;
    private TeacherEntity teacher;

    // Null for a lecture, which the whole cohort attends together
    private StudentGroupEntity studentGroup;

    // LECTURE, TUTORIAL or WORKSHOP
    private String sessionType;

    private int durationMinutes;

    // Students who will sit in the room, the figure capacity is checked against
    private int headcount;

    // Workshops need machines, so their room choice is far narrower
    private boolean needsComputers;

    /** Separates repeat copies of the same thing, e.g. the second lecture of a week. */
    private int copyIndex;

    // Legal placements found during preprocessing, smallest domain gets scheduled first
    @Builder.Default
    private int domainSize = 0;

    public String describe() {
        String audience = studentGroup == null ? batch.getBatchName() : studentGroup.getGroupName();
        return module.getModuleCode() + " " + sessionType.toLowerCase() + " (" + audience + ")";
    }
}
