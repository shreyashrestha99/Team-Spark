package com.Backend.Backend.dto.analytics;

import lombok.*;

import java.util.List;

/** Everything the admin analytics dashboard plots, computed in one pass. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnalyticsDto {

    // Headline counts
    private long students;
    private long teachers;
    private long staff;
    private long rooms;
    private long modules;
    private long programmes;
    private long batches;
    private long sessions;
    private long exams;

    // Room utilisation across the weekly grid
    private double roomUtilisationPct;
    private int teachingSlotsPerWeek;
    private int weeksScheduled;
    private List<LabelledValueDto> roomUtilisation;

    // Shape of the timetable
    private List<LabelledValueDto> sessionsByDay;
    private List<LabelledValueDto> sessionsByPeriod;
    private List<LabelledValueDto> sessionTypeSplit;

    // Cohort spread
    private List<LabelledValueDto> studentsByProgramme;

    // Faculty workload
    private List<TeacherLoadDto> teacherWorkload;
    private int overloadedTeachers;
    private double averageWeeklyHours;

    // Physical capacity
    private int totalSeats;
    private int largestRoomCapacity;
    private double averageRoomFillPct;
}
