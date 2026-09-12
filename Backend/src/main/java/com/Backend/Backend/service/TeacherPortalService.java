package com.Backend.Backend.service;

import com.Backend.Backend.dto.portal.InvigilationDutyDto;
import com.Backend.Backend.dto.portal.MyProfileDto;
import com.Backend.Backend.dto.portal.TeacherDashboardDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.InvigilatorEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.repository.ExamRoomRepository;
import com.Backend.Backend.repository.InvigilatorRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * What a lecturer sees: the classes they teach and the exams they must invigilate.
 *
 * The weekly hours figure is the same one the workload cap is judged against, so a
 * lecturer finds out they are overloaded from their own dashboard rather than after
 * the timetable is published.
 */
@Service
@RequiredArgsConstructor
public class TeacherPortalService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final TimetableSessionRepository sessionRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final ExamRoomRepository examRoomRepository;
    private final TimetableSessionService sessionService;

    /** Landing view: today's teaching, this week's load, and the next invigilation duty. */
    @Transactional(readOnly = true)
    public TeacherDashboardDto getDashboard(String username) {
        UserEntity user = findUserOrThrow(username);
        TeacherEntity teacher = findTeacherOrThrow(user);
        LocalDate today = LocalDate.now();

        List<TimetableSessionResponseDto> todaysClasses = routineBetween(teacher, today, today);
        List<TimetableSessionResponseDto> weekAhead =
                routineBetween(teacher, startOfWeek(today), startOfWeek(today).plusDays(6));

        List<InvigilationDutyDto> duties = getMyDuties(username);
        List<InvigilationDutyDto> upcoming = duties.stream().filter(InvigilationDutyDto::isUpcoming).toList();

        double contactMinutes = weekAhead.stream()
                .mapToLong(TimetableSessionResponseDto::getDurationMinutes)
                .sum();
        double contactHours = Math.round(contactMinutes / 60.0 * 100.0) / 100.0;

        int maxWeekly = teacher.getMaxWeeklyHours() != null ? teacher.getMaxWeeklyHours() : 20;

        long modules = weekAhead.stream()
                .map(TimetableSessionResponseDto::getModuleId)
                .distinct()
                .count();

        return TeacherDashboardDto.builder()
                .profile(buildProfile(user, teacher))
                .todaysClasses(todaysClasses)
                .weekAhead(weekAhead)
                .upcomingDuties(upcoming)
                .nextDuty(upcoming.isEmpty() ? null : upcoming.get(0))
                .classesThisWeek(weekAhead.size())
                .contactHoursThisWeek(contactHours)
                .overloaded(contactHours > maxWeekly)
                .modulesTaught((int) modules)
                .upcomingDutyCount(upcoming.size())
                .build();
    }

    /** The lecturer's own teaching routine, defaulting to the current week. */
    @Transactional(readOnly = true)
    public List<TimetableSessionResponseDto> getMyRoutine(String username, LocalDate fromDate, LocalDate toDate) {
        TeacherEntity teacher = findTeacherOrThrow(findUserOrThrow(username));

        LocalDate start = fromDate != null ? fromDate : startOfWeek(LocalDate.now());
        LocalDate end = toDate != null ? toDate : start.plusDays(6);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return routineBetween(teacher, start, end);
    }

    /** Every exam this lecturer has been rostered to invigilate. */
    @Transactional(readOnly = true)
    public List<InvigilationDutyDto> getMyDuties(String username) {
        UserEntity user = findUserOrThrow(username);
        LocalDate today = LocalDate.now();

        return invigilatorRepository.findMyDuties(user.getUserId()).stream()
                .map(duty -> toDuty(duty, today))
                .toList();
    }

    private List<TimetableSessionResponseDto> routineBetween(TeacherEntity teacher,
                                                             LocalDate fromDate, LocalDate toDate) {
        return sessionService.toDtos(
                sessionRepository.findTeacherRoutine(teacher.getTeacherId(), fromDate, toDate));
    }

    // One roster row becomes a duty card, with the halls to cover
    private InvigilationDutyDto toDuty(InvigilatorEntity duty, LocalDate today) {
        ExamEntity exam = duty.getExam();

        List<ExamRoomEntity> examRooms = examRoomRepository.findAllByExam_ExamId(exam.getExamId());

        List<String> rooms = examRooms.stream()
                .map(examRoom -> {
                    RoomEntity room = examRoom.getRoom();
                    String building = room.getBuilding() != null
                            ? room.getBuilding().getBuildingName() + " · "
                            : "";
                    return building + room.getRoomCode();
                })
                .sorted()
                .toList();

        int candidates = examRooms.stream()
                .mapToInt(examRoom -> examRoom.getAllocatedCapacity() != null
                        ? examRoom.getAllocatedCapacity()
                        : 0)
                .sum();

        return InvigilationDutyDto.builder()
                .invigilatorId(duty.getInvigilatorId())
                .examId(exam.getExamId())
                .moduleCode(exam.getModule().getModuleCode())
                .moduleName(exam.getModule().getModuleName())
                .batchName(exam.getBatch().getBatchName())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .role(duty.getRole())
                .rooms(rooms)
                .candidateCount(candidates)
                .upcoming(!exam.getExamDate().isBefore(today))
                .daysAway(ChronoUnit.DAYS.between(today, exam.getExamDate()))
                .build();
    }

    private MyProfileDto buildProfile(UserEntity user, TeacherEntity teacher) {
        return MyProfileDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .teacherId(teacher.getTeacherId())
                .department(teacher.getDepartment())
                .designation(teacher.getDesignation())
                .maxWeeklyHours(teacher.getMaxWeeklyHours())
                .build();
    }

    // Nepal teaches Sunday to Friday, so a week starts on Sunday
    private LocalDate startOfWeek(LocalDate date) {
        int daysSinceSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY
                ? 0
                : date.getDayOfWeek().getValue();
        return date.minusDays(daysSinceSunday);
    }

    private UserEntity findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private TeacherEntity findTeacherOrThrow(UserEntity user) {
        return teacherRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No lecturer record is linked to this account."));
    }
}
