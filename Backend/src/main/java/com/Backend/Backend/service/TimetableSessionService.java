package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.timetable.ScheduleConflictDto;
import com.Backend.Backend.dto.timetable.TimetableSessionRequestDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.TimetableSessionEntity;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ModuleRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StudentGroupRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableSessionService {

    private final TimetableSessionRepository sessionRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentRepository studentRepository;

    // Create a clash-free session
    @Transactional
    public TimetableSessionResponseDto create(TimetableSessionRequestDto request) {
        validateTimes(request);
        rejectIfConflicting(request, null);

        TimetableSessionEntity session = TimetableSessionEntity.builder()
                .batch(findBatchOrThrow(request.getBatchId()))
                .module(findModuleOrThrow(request.getModuleId()))
                .teacher(findTeacherOrThrow(request.getTeacherId()))
                .room(findRoomOrThrow(request.getRoomId()))
                .studentGroup(findGroupOrNull(request.getGroupId()))
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .sessionType(trimOrNull(request.getSessionType()))
                .status(hasText(request.getStatus()) ? request.getStatus().trim() : "SCHEDULED")
                .build();

        return mapToDto(sessionRepository.save(session));
    }

    // Paged timetable with every filter optional
    @Transactional(readOnly = true)
    public PageResponseDto<TimetableSessionResponseDto> getAll(
            UUID batchId,
            UUID moduleId,
            UUID teacherId,
            UUID roomId,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TimetableSessionEntity> result = sessionRepository.searchSessions(
                batchId, moduleId, teacherId, roomId, fromDate, toDate, blankIfNull(status), pageable
        );

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Fetch one session by id
    @Transactional(readOnly = true)
    public TimetableSessionResponseDto getById(UUID sessionId) {
        return mapToDto(findOrThrow(sessionId));
    }

    // Update, re-checking for clashes
    @Transactional
    public TimetableSessionResponseDto update(UUID sessionId, TimetableSessionRequestDto request) {
        TimetableSessionEntity session = findOrThrow(sessionId);

        validateTimes(request);
        rejectIfConflicting(request, sessionId);

        session.setBatch(findBatchOrThrow(request.getBatchId()));
        session.setModule(findModuleOrThrow(request.getModuleId()));
        session.setTeacher(findTeacherOrThrow(request.getTeacherId()));
        session.setRoom(findRoomOrThrow(request.getRoomId()));
        session.setStudentGroup(findGroupOrNull(request.getGroupId()));
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setSessionType(trimOrNull(request.getSessionType()));
        if (hasText(request.getStatus())) {
            session.setStatus(request.getStatus().trim());
        }

        return mapToDto(sessionRepository.save(session));
    }

    // Remove a session from the timetable
    @Transactional
    public void delete(UUID sessionId) {
        sessionRepository.delete(findOrThrow(sessionId));
    }

    /**
     * Reports clashes for a proposed slot without saving anything.
     * Lets the UI warn before the admin commits.
     */
    @Transactional(readOnly = true)
    public List<ScheduleConflictDto> checkConflicts(TimetableSessionRequestDto request, UUID excludeId) {
        validateTimes(request);
        return detectConflicts(request, excludeId);
    }

    // Block the save when any clash exists
    private void rejectIfConflicting(TimetableSessionRequestDto request, UUID excludeId) {
        List<ScheduleConflictDto> conflicts = detectConflicts(request, excludeId);
        if (!conflicts.isEmpty()) {
            String summary = conflicts.stream()
                    .map(ScheduleConflictDto::getMessage)
                    .collect(Collectors.joining(" | "));
            throw new IllegalArgumentException("Scheduling conflict: " + summary);
        }
    }

    // Core clash detection for one slot
    private List<ScheduleConflictDto> detectConflicts(TimetableSessionRequestDto request, UUID excludeId) {
        List<ScheduleConflictDto> conflicts = new ArrayList<>();

        List<TimetableSessionEntity> clashes = sessionRepository.findClashes(
                request.getSessionDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRoomId(),
                request.getTeacherId(),
                request.getBatchId(),
                request.getGroupId(),
                excludeId
        );

        for (TimetableSessionEntity clash : clashes) {
            String slot = clash.getStartTime() + "–" + clash.getEndTime();

            if (clash.getRoom().getRoomId().equals(request.getRoomId())) {
                conflicts.add(ScheduleConflictDto.builder()
                        .type("ROOM_DOUBLE_BOOKED")
                        .message("Room " + clash.getRoom().getRoomCode() + " is already booked " + slot)
                        .conflictingSessionId(clash.getSessionId())
                        .build());
            }

            if (clash.getTeacher().getTeacherId().equals(request.getTeacherId())) {
                conflicts.add(ScheduleConflictDto.builder()
                        .type("TEACHER_BUSY")
                        .message("Lecturer " + clash.getTeacher().getUser().getFullName()
                                + " already teaches " + slot)
                        .conflictingSessionId(clash.getSessionId())
                        .build());
            }

            if (audiencesOverlap(clash, request.getBatchId(), request.getGroupId())) {
                conflicts.add(ScheduleConflictDto.builder()
                        .type("BATCH_OVERLAP")
                        .message(audienceLabel(clash) + " already has a session " + slot)
                        .conflictingSessionId(clash.getSessionId())
                        .build());
            }
        }

        conflicts.addAll(detectCapacityConflict(request));
        return conflicts;
    }

    /**
     * Two sessions collide for students when they share a batch and their group audiences meet.
     * A whole-batch lecture carries a null group, so it collides with every group of that batch.
     */
    private boolean audiencesOverlap(TimetableSessionEntity clash, UUID batchId, UUID groupId) {
        if (!clash.getBatch().getBatchId().equals(batchId)) {
            return false;
        }

        StudentGroupEntity clashGroup = clash.getStudentGroup();
        return clashGroup == null || groupId == null || clashGroup.getGroupId().equals(groupId);
    }

    // Names whichever audience the clashing session belongs to
    private String audienceLabel(TimetableSessionEntity clash) {
        StudentGroupEntity group = clash.getStudentGroup();
        return group == null
                ? "Batch " + clash.getBatch().getBatchName()
                : clash.getBatch().getBatchName() + " " + group.getGroupName();
    }

    // Warn when the audience outgrows the room, counting only the group when one is set
    private List<ScheduleConflictDto> detectCapacityConflict(TimetableSessionRequestDto request) {
        RoomEntity room = findRoomOrThrow(request.getRoomId());
        Integer capacity = room.getCapacity();

        if (capacity == null) {
            return List.of();
        }

        int students;
        String audience;

        if (request.getGroupId() != null) {
            StudentGroupEntity group = findGroupOrNull(request.getGroupId());
            students = studentRepository.countByStudentGroup_GroupId(request.getGroupId());
            audience = group != null ? group.getGroupName() : "group";
        } else {
            BatchEntity batch = findBatchOrThrow(request.getBatchId());
            students = studentRepository.countByBatch_BatchId(request.getBatchId());
            audience = "batch " + batch.getBatchName();
        }

        if (students > capacity) {
            return List.of(ScheduleConflictDto.builder()
                    .type("CAPACITY_EXCEEDED")
                    .message("Room " + room.getRoomCode() + " holds " + capacity
                            + " but " + audience + " has " + students + " students")
                    .build());
        }

        return List.of();
    }

    // End time must follow start time
    private void validateTimes(TimetableSessionRequestDto request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private TimetableSessionEntity findOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    private BatchEntity findBatchOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    private ModuleEntity findModuleOrThrow(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
    }

    private TeacherEntity findTeacherOrThrow(UUID teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
    }

    private RoomEntity findRoomOrThrow(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
    }

    // Null means the whole batch attends, which is how a lecture is stored
    private StudentGroupEntity findGroupOrNull(UUID groupId) {
        if (groupId == null) {
            return null;
        }
        return studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Empty string keeps the query parameter typed
    private String blankIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Entity to response DTO
    private TimetableSessionResponseDto mapToDto(TimetableSessionEntity session) {
        BatchEntity batch = session.getBatch();
        ModuleEntity module = session.getModule();
        TeacherEntity teacher = session.getTeacher();
        RoomEntity room = session.getRoom();
        StudentGroupEntity group = session.getStudentGroup();
        TimeSlotEntity slot = session.getTimeSlot();

        return TimetableSessionResponseDto.builder()
                .sessionId(session.getSessionId())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .moduleId(module != null ? module.getModuleId() : null)
                .moduleCode(module != null ? module.getModuleCode() : null)
                .moduleName(module != null ? module.getModuleName() : null)
                .teacherId(teacher != null ? teacher.getTeacherId() : null)
                .teacherName(teacher != null && teacher.getUser() != null ? teacher.getUser().getFullName() : null)
                .roomId(room != null ? room.getRoomId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .roomName(room != null ? room.getRoomName() : null)
                .roomCapacity(room != null ? room.getCapacity() : null)
                .groupId(group != null ? group.getGroupId() : null)
                .groupName(group != null ? group.getGroupName() : null)
                .slotId(slot != null ? slot.getSlotId() : null)
                .periodNumber(slot != null ? slot.getPeriodNumber() : null)
                .dayOfWeek(slot != null ? slot.getDayOfWeek() : session.getSessionDate().getDayOfWeek())
                .runId(session.getGenerationRun() != null ? session.getGenerationRun().getRunId() : null)
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .sessionType(session.getSessionType())
                .status(session.getStatus())
                .durationMinutes(Duration.between(session.getStartTime(), session.getEndTime()).toMinutes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
