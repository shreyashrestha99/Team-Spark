package com.Backend.Backend.dto.portal;

import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import lombok.*;

import java.util.List;

/** Everything a student sees the moment they sign in. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardDto {

    private MyProfileDto profile;

    // Classes happening today, so the first thing on screen is where to be now
    private List<TimetableSessionResponseDto> todaysClasses;

    // The rest of this week, for context
    private List<TimetableSessionResponseDto> weekAhead;

    private List<MyExamSeatDto> upcomingExams;

    // The very next exam, pulled out so the seat can be shown large
    private MyExamSeatDto nextExam;

    private int classesThisWeek;
    private double contactHoursThisWeek;
    private int upcomingExamCount;
}
