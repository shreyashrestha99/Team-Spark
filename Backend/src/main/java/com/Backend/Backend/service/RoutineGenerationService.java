package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.generation.GenerateRoutineRequestDto;
import com.Backend.Backend.dto.generation.GenerationResultDto;
import com.Backend.Backend.dto.generation.GenerationRunResponseDto;
import com.Backend.Backend.dto.generation.UnplacedRequirementDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.BatchModuleEntity;
import com.Backend.Backend.entity.GenerationRunEntity;
import com.Backend.Backend.entity.HolidayEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.TimetableSessionEntity;
import com.Backend.Backend.repository.BatchModuleRepository;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.GenerationRunRepository;
import com.Backend.Backend.repository.HolidayRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StudentGroupRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherAvailabilityRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimeSlotRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import com.Backend.Backend.scheduling.GenerationOutcome;
import com.Backend.Backend.scheduling.PlannedSession;
import com.Backend.Backend.scheduling.SchedulingContext;
import com.Backend.Backend.scheduling.SessionRequirement;
import com.Backend.Backend.scheduling.TimetableGenerator;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Turns the delivery patterns on a batch into a real routine.
 *
 * Expands the demand, hands it to the solver, then stamps the one weekly pattern the
 * solver settled on across every week of the semester, skipping holidays. Solving a
 * 25 piece puzzle once and repeating it beats solving a 350 piece puzzle.
 */
@Service
@RequiredArgsConstructor
public class RoutineGenerationService {

    private final TimetableGenerator generator;
    private final GenerationRunRepository generationRunRepository;
    private final TimetableSessionRepository sessionRepository;
    private final BatchRepository batchRepository;
    private final BatchModuleRepository batchModuleRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final HolidayRepository holidayRepository;

    /** Generates and saves a clash-free routine for one batch. */
    @Transactional
    public GenerationResultDto generate(GenerateRoutineRequestDto request) {
        long startedAt = System.currentTimeMillis();

        BatchEntity batch = findBatchOrThrow(request.getBatchId());
        validateDates(request);

        boolean replaceExisting = request.getReplaceExisting() == null || request.getReplaceExisting();
        boolean optimise = request.getOptimise() == null || request.getOptimise();

        if (replaceExisting) {
            sessionRepository.deleteAllByBatch_BatchIdAndSessionDateBetween(
                    batch.getBatchId(), request.getFromDate(), request.getToDate());
            sessionRepository.flush();
        }

        List<SessionRequirement> requirements = buildRequirements(batch);
        if (requirements.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nothing to schedule: assign modules to this batch and set their weekly pattern first."
            );
        }

        SchedulingContext context = buildContext(batch, request.getFromDate(), request.getToDate());
        GenerationOutcome outcome = generator.generate(context, requirements, optimise);

        GenerationRunEntity run = saveRun(batch, request, requirements.size(), outcome);
        List<TimetableSessionEntity> created = materialise(context, outcome.getPlan(), run, request);

        run.setSessionsCreated(created.size());
        generationRunRepository.save(run);

        return GenerationResultDto.builder()
                .run(mapRunToDto(run))
                .weeklyPattern(weeklyPatternOf(context, outcome.getPlan()))
                .unplaced(mapUnplaced(outcome))
                .penaltyBeforeOptimise(outcome.getPenaltyBeforeOptimise())
                .penaltyAfterOptimise(outcome.getPenaltyAfterOptimise())
                .idleGapsBefore(outcome.getIdleGapsBefore())
                .idleGapsAfter(outcome.getIdleGapsAfter())
                .generationMillis(System.currentTimeMillis() - startedAt)
                .build();
    }

    // Paged history of previous runs
    @Transactional(readOnly = true)
    public PageResponseDto<GenerationRunResponseDto> getRuns(
            UUID batchId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<GenerationRunEntity> result = generationRunRepository.searchRuns(batchId, pageable);
        return PageResponseDto.from(result.map(this::mapRunToDto));
    }

    // Fetch one run by id
    @Transactional(readOnly = true)
    public GenerationRunResponseDto getRunById(UUID runId) {
        return mapRunToDto(findRunOrThrow(runId));
    }

    /** Rolls a whole generated routine back, which is why every session carries its run id. */
    @Transactional
    public void rollback(UUID runId) {
        GenerationRunEntity run = findRunOrThrow(runId);
        sessionRepository.deleteAllByGenerationRun_RunId(runId);
        generationRunRepository.delete(run);
    }

    /**
     * Expands each delivery pattern into the individual sessions that must be placed.
     * One module with a lecture, a tutorial and a workshop across two groups becomes
     * five requirements: the shared lecture plus a tutorial and a workshop per group.
     */
    private List<SessionRequirement> buildRequirements(BatchEntity batch) {
        List<BatchModuleEntity> batchModules = batchModuleRepository.findAllByBatch_BatchId(batch.getBatchId());
        List<StudentGroupEntity> groups =
                studentGroupRepository.findAllByBatch_BatchIdOrderByGroupNameAsc(batch.getBatchId());

        int batchHeadcount = Math.max(studentRepository.countByBatch_BatchId(batch.getBatchId()), 1);
        Map<UUID, Integer> loadByTeacher = new HashMap<>();
        List<SessionRequirement> requirements = new ArrayList<>();

        for (BatchModuleEntity batchModule : batchModules) {
            TeacherEntity teacher = resolveTeacher(batchModule, loadByTeacher);

            addRequirements(requirements, batch, batchModule, teacher, "LECTURE",
                    batchModule.getLectureSessionsPerWeek(), batchModule.getLectureDurationMinutes(),
                    false, groups, batchHeadcount, false, loadByTeacher);

            addRequirements(requirements, batch, batchModule, teacher, "TUTORIAL",
                    batchModule.getTutorialSessionsPerWeek(), batchModule.getTutorialDurationMinutes(),
                    Boolean.TRUE.equals(batchModule.getSplitTutorialByGroup()), groups, batchHeadcount,
                    false, loadByTeacher);

            addRequirements(requirements, batch, batchModule, teacher, "WORKSHOP",
                    batchModule.getWorkshopSessionsPerWeek(), batchModule.getWorkshopDurationMinutes(),
                    Boolean.TRUE.equals(batchModule.getSplitWorkshopByGroup()), groups, batchHeadcount,
                    true, loadByTeacher);
        }

        return requirements;
    }

    // One entry per copy, and one copy per group when the type is split
    private void addRequirements(List<SessionRequirement> target, BatchEntity batch, BatchModuleEntity batchModule,
                                 TeacherEntity teacher, String sessionType, int sessionsPerWeek, int durationMinutes,
                                 boolean splitByGroup, List<StudentGroupEntity> groups, int batchHeadcount,
                                 boolean needsComputers, Map<UUID, Integer> loadByTeacher) {
        if (sessionsPerWeek <= 0) {
            return;
        }

        List<StudentGroupEntity> audiences = splitByGroup && !groups.isEmpty()
                ? groups
                : java.util.Collections.singletonList(null);

        for (int copy = 0; copy < sessionsPerWeek; copy++) {
            for (StudentGroupEntity group : audiences) {
                int headcount = group == null
                        ? batchHeadcount
                        : Math.max(studentRepository.countByStudentGroup_GroupId(group.getGroupId()), 1);

                target.add(SessionRequirement.builder()
                        .batch(batch)
                        .module(batchModule.getModule())
                        .teacher(teacher)
                        .studentGroup(group)
                        .sessionType(sessionType)
                        .durationMinutes(durationMinutes)
                        .headcount(headcount)
                        .needsComputers(needsComputers)
                        .copyIndex(copy)
                        .build());

                loadByTeacher.merge(teacher.getTeacherId(), durationMinutes, Integer::sum);
            }
        }
    }

    /**
     * Uses the lecturer named on the batch module, or the lightest loaded one when none is set,
     * which keeps teaching hours spread rather than piled on whoever happens to be first.
     */
    private TeacherEntity resolveTeacher(BatchModuleEntity batchModule, Map<UUID, Integer> loadByTeacher) {
        if (batchModule.getTeacher() != null) {
            return batchModule.getTeacher();
        }

        List<TeacherEntity> teachers = teacherRepository.findAll();
        if (teachers.isEmpty()) {
            throw new IllegalArgumentException(
                    "No lecturers exist. Add teaching staff before generating a routine."
            );
        }

        return teachers.stream()
                .min(java.util.Comparator.comparingInt(
                        teacher -> loadByTeacher.getOrDefault(teacher.getTeacherId(), 0)))
                .orElse(teachers.get(0));
    }

    // Loads the grid, the rooms and everything already booked in the window
    private SchedulingContext buildContext(BatchEntity batch, LocalDate fromDate, LocalDate toDate) {
        List<TimeSlotEntity> slots =
                timeSlotRepository.findAllByIsTeachingSlotTrueOrderByDayOfWeekAscPeriodNumberAsc();

        if (slots.isEmpty()) {
            throw new IllegalArgumentException(
                    "The weekly grid is empty. Create time slots before generating a routine."
            );
        }

        List<RoomEntity> rooms = roomRepository.findAllByIsAvailableTrueOrderByRoomCodeAsc();
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("No rooms are available to schedule into.");
        }

        Map<UUID, Set<UUID>> groupsByBatch = new HashMap<>();
        for (StudentGroupEntity group : studentGroupRepository.findAll()) {
            groupsByBatch
                    .computeIfAbsent(group.getBatch().getBatchId(), key -> new HashSet<>())
                    .add(group.getGroupId());
        }

        SchedulingContext context = new SchedulingContext(
                slots, rooms, availabilityRepository.findAll(), groupsByBatch);

        // Other batches already own some of these rooms and lecturers, so block those cells
        for (TimetableSessionEntity existing : sessionRepository.findAllInWindow(fromDate, toDate)) {
            if (!existing.getBatch().getBatchId().equals(batch.getBatchId())) {
                context.occupyExisting(existing);
            }
        }

        return context;
    }

    /**
     * Stamps the weekly pattern across every week in the window.
     * A holiday simply loses its copy of that day, the rest of the routine is untouched.
     */
    private List<TimetableSessionEntity> materialise(SchedulingContext context, List<PlannedSession> plan,
                                                     GenerationRunEntity run, GenerateRoutineRequestDto request) {
        Set<LocalDate> holidays = holidayRepository
                .findAllByHolidayDateBetween(request.getFromDate(), request.getToDate()).stream()
                .map(HolidayEntity::getHolidayDate)
                .collect(Collectors.toSet());

        List<TimetableSessionEntity> created = new ArrayList<>();

        for (LocalDate date = request.getFromDate(); !date.isAfter(request.getToDate()); date = date.plusDays(1)) {
            if (holidays.contains(date)) {
                continue;
            }

            for (PlannedSession planned : plan) {
                TimeSlotEntity startSlot = context.slotAt(planned.getStartSlotIndex());
                if (startSlot.getDayOfWeek() != date.getDayOfWeek()) {
                    continue;
                }

                created.add(toSession(context, planned, run, date));
            }
        }

        return sessionRepository.saveAll(created);
    }

    // One planned slot becomes one dated session row
    private TimetableSessionEntity toSession(SchedulingContext context, PlannedSession planned,
                                             GenerationRunEntity run, LocalDate date) {
        SessionRequirement requirement = planned.getRequirement();
        TimeSlotEntity startSlot = context.slotAt(planned.getStartSlotIndex());
        TimeSlotEntity endSlot = context.slotAt(planned.endSlotIndexExclusive() - 1);

        return TimetableSessionEntity.builder()
                .batch(requirement.getBatch())
                .module(requirement.getModule())
                .teacher(requirement.getTeacher())
                .room(planned.getRoom())
                .studentGroup(requirement.getStudentGroup())
                .timeSlot(startSlot)
                .generationRun(run)
                .sessionDate(date)
                .startTime(startSlot.getStartTime())
                .endTime(endSlot.getEndTime())
                .sessionType(requirement.getSessionType())
                .status("SCHEDULED")
                .build();
    }

    // Saves the run summary before the sessions, so every session can point at it
    private GenerationRunEntity saveRun(BatchEntity batch, GenerateRoutineRequestDto request,
                                        int requirementCount, GenerationOutcome outcome) {
        String notes = outcome.isComplete()
                ? "Every session was placed."
                : outcome.getUnplaced().entrySet().stream()
                .map(entry -> entry.getKey().describe() + ": " + entry.getValue())
                .collect(Collectors.joining(" | "));

        return generationRunRepository.save(GenerationRunEntity.builder()
                .batch(batch)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .status(outcome.isComplete() ? "COMPLETED" : "PARTIAL")
                .requirementsTotal(requirementCount)
                .requirementsPlaced(outcome.getPlan().size())
                .penaltyScore(outcome.getPenaltyAfterOptimise())
                .sessionsCreated(0)
                .notes(notes.length() > 4000 ? notes.substring(0, 3997) + "..." : notes)
                .build());
    }

    // The single week the whole routine repeats, handy for rendering the grid
    private List<TimetableSessionResponseDto> weeklyPatternOf(SchedulingContext context,
                                                              List<PlannedSession> plan) {
        return plan.stream()
                .map(planned -> {
                    SessionRequirement requirement = planned.getRequirement();
                    TimeSlotEntity startSlot = context.slotAt(planned.getStartSlotIndex());
                    TimeSlotEntity endSlot = context.slotAt(planned.endSlotIndexExclusive() - 1);
                    RoomEntity room = planned.getRoom();
                    StudentGroupEntity group = requirement.getStudentGroup();

                    return TimetableSessionResponseDto.builder()
                            .batchId(requirement.getBatch().getBatchId())
                            .batchName(requirement.getBatch().getBatchName())
                            .moduleId(requirement.getModule().getModuleId())
                            .moduleCode(requirement.getModule().getModuleCode())
                            .moduleName(requirement.getModule().getModuleName())
                            .teacherId(requirement.getTeacher().getTeacherId())
                            .teacherName(requirement.getTeacher().getUser() != null
                                    ? requirement.getTeacher().getUser().getFullName()
                                    : null)
                            .roomId(room.getRoomId())
                            .roomCode(room.getRoomCode())
                            .roomName(room.getRoomName())
                            .roomCapacity(room.getCapacity())
                            .groupId(group != null ? group.getGroupId() : null)
                            .groupName(group != null ? group.getGroupName() : null)
                            .slotId(startSlot.getSlotId())
                            .periodNumber(startSlot.getPeriodNumber())
                            .dayOfWeek(startSlot.getDayOfWeek())
                            .startTime(startSlot.getStartTime())
                            .endTime(endSlot.getEndTime())
                            .sessionType(requirement.getSessionType())
                            .status("SCHEDULED")
                            .durationMinutes(Duration.between(
                                    startSlot.getStartTime(), endSlot.getEndTime()).toMinutes())
                            .build();
                })
                .toList();
    }

    // Anything that could not be placed, with the reason the solver gave
    private List<UnplacedRequirementDto> mapUnplaced(GenerationOutcome outcome) {
        return outcome.getUnplaced().entrySet().stream()
                .map(entry -> {
                    SessionRequirement requirement = entry.getKey();
                    return UnplacedRequirementDto.builder()
                            .moduleCode(requirement.getModule().getModuleCode())
                            .moduleName(requirement.getModule().getModuleName())
                            .sessionType(requirement.getSessionType())
                            .groupName(requirement.getStudentGroup() != null
                                    ? requirement.getStudentGroup().getGroupName()
                                    : null)
                            .durationMinutes(requirement.getDurationMinutes())
                            .reason(entry.getValue())
                            .build();
                })
                .toList();
    }

    // Start date must precede end date
    private void validateDates(GenerateRoutineRequestDto request) {
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private BatchEntity findBatchOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    private GenerationRunEntity findRunOrThrow(UUID runId) {
        return generationRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Generation run not found: " + runId));
    }

    // Entity to response DTO
    private GenerationRunResponseDto mapRunToDto(GenerationRunEntity run) {
        BatchEntity batch = run.getBatch();

        return GenerationRunResponseDto.builder()
                .runId(run.getRunId())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .fromDate(run.getFromDate())
                .toDate(run.getToDate())
                .status(run.getStatus())
                .requirementsTotal(run.getRequirementsTotal())
                .requirementsPlaced(run.getRequirementsPlaced())
                .penaltyScore(run.getPenaltyScore())
                .sessionsCreated(run.getSessionsCreated())
                .notes(run.getNotes())
                .createdAt(run.getCreatedAt())
                .updatedAt(run.getUpdatedAt())
                .build();
    }
}
