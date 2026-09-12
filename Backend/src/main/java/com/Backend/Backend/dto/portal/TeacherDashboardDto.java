package com.Backend.Backend.dto.portal;

import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import lombok.*;

import java.util.List;

/** Everything a lecturer sees the moment they sign in. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDashboardDto {

    private MyProfileDto profile;

    private List<TimetableSessionResponseDto> todaysClasses;
    private List<TimetableSessionResponseDto> weekAhead;

    private List<InvigilationDutyDto> upcomingDuties;
    private InvigilationDutyDto nextDuty;

    private int classesThisWeek;

    /** Teaching hours this week, the figure checked against the weekly cap. */
    private double contactHoursThisWeek;

    // True once the week exceeds the lecturer's own maximum
    private boolean overloaded;

    private int modulesTaught;
    private int upcomingDutyCount;
}
