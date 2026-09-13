package com.Backend.Backend.service;

import com.Backend.Backend.dto.analytics.AdminAnalyticsDto;
import com.Backend.Backend.dto.analytics.LabelledValueDto;
import com.Backend.Backend.dto.analytics.TeacherLoadDto;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.TimetableSessionEntity;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.ModuleRepository;
import com.Backend.Backend.repository.ProgrammeRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StaffRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimeSlotRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Derives the institutional picture the admin dashboard plots.
 *
 * Everything is computed from what is already scheduled, so the numbers move as
 * the routine does rather than being stored and drifting out of date.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int TOP_ROOMS = 8;
    private static final int TOP_TEACHERS = 8;

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StaffRepository staffRepository;
    private final RoomRepository roomRepository;
    private final ModuleRepository moduleRepository;
    private final ProgrammeRepository programmeRepository;
    private final BatchRepository batchRepository;
    private final ExamRepository examRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimetableSessionRepository sessionRepository;

    // One pass over the scheduled data
    @Transactional(readOnly = true)
    public AdminAnalyticsDto getAdminAnalytics() {
        List<TimetableSessionEntity> sessions = sessionRepository.findAll();
        List<RoomEntity> rooms = roomRepository.findAll();
        List<TimeSlotEntity> teachingSlots = timeSlotRepository
                .findAllByIsTeachingSlotTrueOrderByDayOfWeekAscPeriodNumberAsc();

        int weeks = distinctWeeks(sessions);

        return AdminAnalyticsDto.builder()
                .students(studentRepository.count())
                .teachers(teacherRepository.count())
                .staff(staffRepository.count())
                .rooms(roomRepository.count())
                .modules(moduleRepository.count())
                .programmes(programmeRepository.count())
                .batches(batchRepository.count())
                .sessions(sessions.size())
                .exams(examRepository.count())
                .teachingSlotsPerWeek(teachingSlots.size())
                .weeksScheduled(weeks)
                .roomUtilisationPct(overallRoomUtilisation(sessions, rooms, teachingSlots, weeks))
                .roomUtilisation(roomUtilisation(sessions, rooms, teachingSlots, weeks))
                .sessionsByDay(sessionsByDay(sessions))
                .sessionsByPeriod(sessionsByPeriod(sessions))
                .sessionTypeSplit(sessionTypeSplit(sessions))
                .studentsByProgramme(studentsByProgramme())
                .teacherWorkload(teacherWorkload(sessions, weeks))
                .overloadedTeachers(countOverloaded(sessions, weeks))
                .averageWeeklyHours(averageWeeklyHours(sessions, weeks))
                .totalSeats(rooms.stream().mapToInt(room -> orZero(room.getCapacity())).sum())
                .largestRoomCapacity(rooms.stream().mapToInt(room -> orZero(room.getCapacity())).max().orElse(0))
                .averageRoomFillPct(averageRoomFill(sessions))
                .build();
    }

    /**
     * How many calendar weeks the routine spans. The weekly pattern repeats, so
     * dividing by this turns totals back into a typical week.
     */
    private int distinctWeeks(List<TimetableSessionEntity> sessions) {
        if (sessions.isEmpty()) {
            return 1;
        }
        WeekFields weekFields = WeekFields.ISO;
        long count = sessions.stream()
                .map(session -> session.getSessionDate().get(weekFields.weekOfWeekBasedYear())
                        + "-" + session.getSessionDate().get(weekFields.weekBasedYear()))
                .distinct()
                .count();
        return (int) Math.max(count, 1);
    }

    // Share of the weekly room-slot supply that is actually booked
    private double overallRoomUtilisation(List<TimetableSessionEntity> sessions, List<RoomEntity> rooms,
                                          List<TimeSlotEntity> teachingSlots, int weeks) {
        int supply = rooms.size() * teachingSlots.size();
        if (supply == 0) {
            return 0;
        }
        double weeklyBooked = sessions.size() / (double) weeks;
        return round1(Math.min(weeklyBooked / supply * 100, 100));
    }

    // Busiest rooms, as a percentage of that room's weekly slots
    private List<LabelledValueDto> roomUtilisation(List<TimetableSessionEntity> sessions, List<RoomEntity> rooms,
                                                   List<TimeSlotEntity> teachingSlots, int weeks) {
        if (teachingSlots.isEmpty() || rooms.isEmpty()) {
            return List.of();
        }

        Map<String, Long> bookedByRoom = sessions.stream()
                .filter(session -> session.getRoom() != null)
                .collect(Collectors.groupingBy(
                        session -> session.getRoom().getRoomCode(), Collectors.counting()));

        return rooms.stream()
                .map(room -> {
                    double weekly = bookedByRoom.getOrDefault(room.getRoomCode(), 0L) / (double) weeks;
                    double pct = Math.min(weekly / teachingSlots.size() * 100, 100);
                    return LabelledValueDto.builder()
                            .label(room.getRoomCode())
                            .value(round1(pct))
                            .detail(Math.round(weekly) + " of " + teachingSlots.size() + " slots")
                            .build();
                })
                .sorted(Comparator.comparingDouble(LabelledValueDto::getValue).reversed())
                .limit(TOP_ROOMS)
                .toList();
    }

    /**
     * Teaching load across the week, in Nepali academic order: the week runs
     * Sunday to Friday with Saturday as the weekend, so Sunday leads.
     */
    private static final List<DayOfWeek> ACADEMIC_WEEK = List.of(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private List<LabelledValueDto> sessionsByDay(List<TimetableSessionEntity> sessions) {
        Map<DayOfWeek, Long> counts = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSessionDate().getDayOfWeek(), Collectors.counting()));

        List<LabelledValueDto> result = new ArrayList<>();
        for (DayOfWeek day : ACADEMIC_WEEK) {
            long value = counts.getOrDefault(day, 0L);
            // Saturday is the weekend here, so only show it if something was actually scheduled
            if (day == DayOfWeek.SATURDAY && value == 0) continue;
            result.add(LabelledValueDto.builder()
                    .label(day.name().substring(0, 3))
                    .value(value)
                    .build());
        }
        return result;
    }

    // Which periods of the day carry the load
    private List<LabelledValueDto> sessionsByPeriod(List<TimetableSessionEntity> sessions) {
        Map<Integer, Long> counts = sessions.stream()
                .filter(session -> session.getTimeSlot() != null
                        && session.getTimeSlot().getPeriodNumber() != null)
                .collect(Collectors.groupingBy(
                        session -> session.getTimeSlot().getPeriodNumber(),
                        Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> LabelledValueDto.builder()
                        .label("P" + entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    // Lecture vs tutorial vs workshop mix
    private List<LabelledValueDto> sessionTypeSplit(List<TimetableSessionEntity> sessions) {
        Map<String, Long> counts = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSessionType() == null ? "OTHER" : session.getSessionType(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> LabelledValueDto.builder()
                        .label(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    // Headcount per degree programme
    private List<LabelledValueDto> studentsByProgramme() {
        Map<String, Long> counts = studentRepository.findAll().stream()
                .map(this::programmeNameOf)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> LabelledValueDto.builder()
                        .label(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    private String programmeNameOf(StudentEntity student) {
        if (student.getBatch() == null || student.getBatch().getProgramme() == null) {
            return "Unassigned";
        }
        return student.getBatch().getProgramme().getProgrammeName();
    }

    // Weekly contact hours per lecturer against their ceiling
    private List<TeacherLoadDto> teacherWorkload(List<TimetableSessionEntity> sessions, int weeks) {
        Map<TeacherEntity, Double> minutesByTeacher = weeklyMinutesByTeacher(sessions, weeks);

        return minutesByTeacher.entrySet().stream()
                .map(entry -> toLoadDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(TeacherLoadDto::getWeeklyHours).reversed())
                .limit(TOP_TEACHERS)
                .toList();
    }

    private TeacherLoadDto toLoadDto(TeacherEntity teacher, double weeklyMinutes) {
        double hours = round1(weeklyMinutes / 60);
        int ceiling = teacher.getMaxWeeklyHours() == null || teacher.getMaxWeeklyHours() <= 0
                ? 20
                : teacher.getMaxWeeklyHours();

        return TeacherLoadDto.builder()
                .name(teacher.getUser() != null ? teacher.getUser().getFullName() : "Unknown")
                .department(teacher.getDepartment())
                .weeklyHours(hours)
                .maxWeeklyHours(ceiling)
                .utilisationPct(round1(hours / ceiling * 100))
                .overloaded(hours > ceiling)
                .build();
    }

    // Shared aggregation so the table and the counters agree
    private Map<TeacherEntity, Double> weeklyMinutesByTeacher(List<TimetableSessionEntity> sessions, int weeks) {
        Map<TeacherEntity, Double> minutes = new LinkedHashMap<>();

        for (TimetableSessionEntity session : sessions) {
            if (session.getTeacher() == null) {
                continue;
            }
            double length = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
            minutes.merge(session.getTeacher(), length / weeks, Double::sum);
        }

        return minutes;
    }

    private int countOverloaded(List<TimetableSessionEntity> sessions, int weeks) {
        return (int) weeklyMinutesByTeacher(sessions, weeks).entrySet().stream()
                .map(entry -> toLoadDto(entry.getKey(), entry.getValue()))
                .filter(TeacherLoadDto::isOverloaded)
                .count();
    }

    private double averageWeeklyHours(List<TimetableSessionEntity> sessions, int weeks) {
        Map<TeacherEntity, Double> minutes = weeklyMinutesByTeacher(sessions, weeks);
        if (minutes.isEmpty()) {
            return 0;
        }
        double total = minutes.values().stream().mapToDouble(Double::doubleValue).sum();
        return round1(total / minutes.size() / 60);
    }

    /**
     * How full the booked rooms actually are. A clash-free timetable that puts
     * twelve students in a hundred-seat hall is still wasting the estate.
     */
    private double averageRoomFill(List<TimetableSessionEntity> sessions) {
        Set<String> counted = new java.util.HashSet<>();
        List<Double> fills = new ArrayList<>();

        for (TimetableSessionEntity session : sessions) {
            if (session.getRoom() == null || session.getBatch() == null) {
                continue;
            }
            Integer capacity = session.getRoom().getCapacity();
            if (capacity == null || capacity <= 0) {
                continue;
            }

            // One reading per weekly pattern cell, not per repeated date
            String key = session.getRoom().getRoomId() + "|" + session.getSessionDate().getDayOfWeek()
                    + "|" + session.getStartTime() + "|" + session.getBatch().getBatchId();
            if (!counted.add(key)) {
                continue;
            }

            int headcount = session.getStudentGroup() != null
                    ? studentRepository.countByStudentGroup_GroupId(session.getStudentGroup().getGroupId())
                    : studentRepository.countByBatch_BatchId(session.getBatch().getBatchId());

            fills.add(Math.min(headcount / (double) capacity * 100, 100));
        }

        if (fills.isEmpty()) {
            return 0;
        }
        return round1(fills.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    private int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
