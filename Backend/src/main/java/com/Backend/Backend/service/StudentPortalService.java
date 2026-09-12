package com.Backend.Backend.service;

import com.Backend.Backend.dto.portal.MyExamSeatDto;
import com.Backend.Backend.dto.portal.MyProfileDto;
import com.Backend.Backend.dto.portal.StudentDashboardDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.SeatAllocationEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.repository.SeatAllocationRepository;
import com.Backend.Backend.repository.StudentRepository;
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
 * What a student sees of the timetable: their own classes and their own desk.
 *
 * Everything is scoped to the signed-in student, so a cohort's other groups and other
 * students' seats are never reachable from here.
 */
@Service
@RequiredArgsConstructor
public class StudentPortalService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TimetableSessionRepository sessionRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final TimetableSessionService sessionService;

    /** Landing view: where to be today, what is coming, and the next exam seat. */
    @Transactional(readOnly = true)
    public StudentDashboardDto getDashboard(String username) {
        StudentEntity student = findStudentOrThrow(username);
        LocalDate today = LocalDate.now();

        List<TimetableSessionResponseDto> todaysClasses = routineBetween(student, today, today);
        List<TimetableSessionResponseDto> weekAhead =
                routineBetween(student, startOfWeek(today), startOfWeek(today).plusDays(6));

        List<MyExamSeatDto> exams = getMyExams(username);
        List<MyExamSeatDto> upcoming = exams.stream().filter(MyExamSeatDto::isUpcoming).toList();

        double contactMinutes = weekAhead.stream()
                .mapToLong(TimetableSessionResponseDto::getDurationMinutes)
                .sum();

        return StudentDashboardDto.builder()
                .profile(buildProfile(student))
                .todaysClasses(todaysClasses)
                .weekAhead(weekAhead)
                .upcomingExams(upcoming)
                .nextExam(upcoming.isEmpty() ? null : upcoming.get(0))
                .classesThisWeek(weekAhead.size())
                .contactHoursThisWeek(Math.round(contactMinutes / 60.0 * 100.0) / 100.0)
                .upcomingExamCount(upcoming.size())
                .build();
    }

    /** The student's own routine, defaulting to the current week. */
    @Transactional(readOnly = true)
    public List<TimetableSessionResponseDto> getMyRoutine(String username, LocalDate fromDate, LocalDate toDate) {
        StudentEntity student = findStudentOrThrow(username);
        LocalDate start = fromDate != null ? fromDate : startOfWeek(LocalDate.now());
        LocalDate end = toDate != null ? toDate : start.plusDays(6);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return routineBetween(student, start, end);
    }

    /** Every exam the student sits, with the hall and desk they were given. */
    @Transactional(readOnly = true)
    public List<MyExamSeatDto> getMyExams(String username) {
        StudentEntity student = findStudentOrThrow(username);
        LocalDate today = LocalDate.now();

        return seatAllocationRepository.findMySeats(student.getStudentId()).stream()
                .map(allocation -> toExamSeat(allocation, today))
                .toList();
    }

    // Sessions for this student's batch, narrowed to their own group
    private List<TimetableSessionResponseDto> routineBetween(StudentEntity student,
                                                             LocalDate fromDate, LocalDate toDate) {
        if (student.getBatch() == null) {
            return List.of();
        }

        StudentGroupEntity group = student.getStudentGroup();

        return sessionService.toDtos(sessionRepository.findStudentRoutine(
                student.getBatch().getBatchId(),
                group != null ? group.getGroupId() : null,
                fromDate,
                toDate
        ));
    }

    // One seat allocation becomes a "where do I sit" card
    private MyExamSeatDto toExamSeat(SeatAllocationEntity allocation, LocalDate today) {
        ExamEntity exam = allocation.getExam();
        ExamRoomEntity examRoom = allocation.getExamRoom();
        RoomEntity room = examRoom != null ? examRoom.getRoom() : null;

        return MyExamSeatDto.builder()
                .examId(exam.getExamId())
                .moduleCode(exam.getModule().getModuleCode())
                .moduleName(exam.getModule().getModuleName())
                .examType(exam.getExamType())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .buildingName(room != null && room.getBuilding() != null
                        ? room.getBuilding().getBuildingName()
                        : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .roomName(room != null ? room.getRoomName() : null)
                .seatNumber(allocation.getSeatNumber())
                .rowLabel(allocation.getRowNumber())
                .columnNumber(allocation.getColumnNumber())
                .status(exam.getStatus())
                .upcoming(!exam.getExamDate().isBefore(today))
                .daysAway(ChronoUnit.DAYS.between(today, exam.getExamDate()))
                .build();
    }

    // Identity plus where the student sits in the programme
    private MyProfileDto buildProfile(StudentEntity student) {
        UserEntity user = student.getUser();
        BatchEntity batch = student.getBatch();
        StudentGroupEntity group = student.getStudentGroup();

        return MyProfileDto.builder()
                .userId(user != null ? user.getUserId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .studentId(student.getStudentId())
                .studentNumber(student.getStudentNumber())
                .registrationNumber(student.getRegistrationNumber())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .groupId(group != null ? group.getGroupId() : null)
                .groupName(group != null ? group.getGroupName() : null)
                .programmeName(batch != null && batch.getProgramme() != null
                        ? batch.getProgramme().getProgrammeName()
                        : null)
                .yearOfStudy(batch != null ? batch.getYearOfStudy() : null)
                .semester(batch != null ? batch.getSemester() : null)
                .build();
    }

    // Nepal teaches Sunday to Friday, so a week starts on Sunday
    private LocalDate startOfWeek(LocalDate date) {
        int daysSinceSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY
                ? 0
                : date.getDayOfWeek().getValue();
        return date.minusDays(daysSinceSunday);
    }

    // Resolves the signed-in user to their student record
    private StudentEntity findStudentOrThrow(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return studentRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No student record is linked to this account."));
    }
}
