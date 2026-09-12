package com.Backend.Backend.service;

import com.Backend.Backend.dto.seating.SeatCellDto;
import com.Backend.Backend.dto.seating.SeatingPlanDto;
import com.Backend.Backend.dto.seating.SeatingRoomDto;
import com.Backend.Backend.entity.BuildingEntity;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.InvigilatorEntity;
import com.Backend.Backend.entity.SeatAllocationEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.ExamRoomRepository;
import com.Backend.Backend.repository.InvigilatorRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.SeatAllocationRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Picks halls for an exam and seats every candidate in them, with no manual typing.
 *
 * Three steps, all ordinary algorithms:
 *   1. Venue choice   first-fit-decreasing bin packing over the halls that are free,
 *                     preferring to keep one exam inside a single block.
 *   2. Seat choice    spaced desks first, so nobody sits directly beside a neighbour
 *                     sitting the same paper.
 *   3. Sharing        a second exam in the same hall falls into the gaps the first left,
 *                     which interleaves the two papers and removes collusion entirely.
 */
@Service
@RequiredArgsConstructor
public class ExamSeatingService {

    // One invigilator covers roughly this many candidates, on top of one per hall
    private static final int CANDIDATES_PER_INVIGILATOR = 30;

    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final TimetableSessionRepository sessionRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final UserRepository userRepository;

    /**
     * Allocates halls and seats for one exam, replacing anything already allocated to it.
     * Safe to run again after the cohort or the room list changes.
     */
    @Transactional
    public SeatingPlanDto autoAllocate(UUID examId) {
        long startedAt = System.currentTimeMillis();

        ExamEntity exam = findExamOrThrow(examId);
        List<String> warnings = new ArrayList<>();

        List<StudentEntity> candidates = studentRepository
                .findAllByBatch_BatchId(exam.getBatch().getBatchId()).stream()
                .sorted(Comparator.comparing(StudentEntity::getStudentNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "No students are enrolled in " + exam.getBatch().getBatchName() + "."
            );
        }

        clearAllocations(exam);

        Map<UUID, Integer> remaining = remainingSeatsByRoom(exam);
        List<RoomEntity> halls = chooseHalls(exam, candidates.size(), warnings);
        if (halls.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hall is free at that time with a seat grid. Set rows and seats per row on your rooms."
            );
        }

        int seated = seatCandidates(exam, halls, candidates, remaining);
        assignInvigilators(exam, warnings);

        if (seated < candidates.size()) {
            warnings.add((candidates.size() - seated) + " candidate(s) have no desk. "
                    + "Free another hall at this time, or raise the exam spacing factor on the rooms used.");
        }

        SeatingPlanDto plan = getPlan(examId);
        plan.setWarnings(warnings);
        plan.setAllocationMillis(System.currentTimeMillis() - startedAt);
        return plan;
    }

    /** The saved plan, with every desk in every hall whether occupied or not. */
    @Transactional(readOnly = true)
    public SeatingPlanDto getPlan(UUID examId) {
        ExamEntity exam = findExamOrThrow(examId);

        List<ExamRoomEntity> examRooms = examRoomRepository.findAllByExam_ExamId(examId);
        int totalStudents = studentRepository.countByBatch_BatchId(exam.getBatch().getBatchId());

        List<SeatingRoomDto> rooms = new ArrayList<>();
        int seated = 0;

        for (ExamRoomEntity examRoom : examRooms) {
            SeatingRoomDto dto = buildRoomPlan(exam, examRoom);
            seated += dto.getSeatedHere();
            rooms.add(dto);
        }

        rooms.sort(Comparator.comparing(SeatingRoomDto::getRoomCode));

        return SeatingPlanDto.builder()
                .examId(exam.getExamId())
                .moduleCode(exam.getModule().getModuleCode())
                .moduleName(exam.getModule().getModuleName())
                .batchName(exam.getBatch().getBatchName())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .totalStudents(totalStudents)
                .seatedStudents(seated)
                .unseatedStudents(Math.max(totalStudents - seated, 0))
                .rooms(rooms)
                .warnings(List.of())
                .build();
    }

    /** Releases every hall and desk held by an exam. */
    @Transactional
    public void clearSeating(UUID examId) {
        clearAllocations(findExamOrThrow(examId));
    }

    private void clearAllocations(ExamEntity exam) {
        seatAllocationRepository.deleteAllByExam_ExamId(exam.getExamId());
        invigilatorRepository.deleteAllByExam_ExamId(exam.getExamId());
        examRoomRepository.deleteAllByExam_ExamId(exam.getExamId());
        seatAllocationRepository.flush();
        invigilatorRepository.flush();
        examRoomRepository.flush();
    }

    /**
     * Rosters invigilators: one per hall plus one for every thirty candidates.
     *
     * Duties go to whoever is carrying the fewest so far, which spreads the load instead
     * of piling it on whoever appears first in the list. Anyone teaching a class at that
     * hour, or already invigilating an overlapping exam, is skipped entirely.
     */
    private void assignInvigilators(ExamEntity exam, List<String> warnings) {
        List<ExamRoomEntity> examRooms = examRoomRepository.findAllByExam_ExamId(exam.getExamId());
        if (examRooms.isEmpty()) {
            return;
        }

        int candidates = examRooms.stream()
                .mapToInt(room -> room.getAllocatedCapacity() != null ? room.getAllocatedCapacity() : 0)
                .sum();

        int needed = examRooms.size()
                + (int) Math.ceil((double) candidates / CANDIDATES_PER_INVIGILATOR);

        Set<UUID> teachingNow = new HashSet<>(sessionRepository.findTeacherUserIdsBusyAt(
                exam.getExamDate(), exam.getStartTime(), exam.getEndTime()));

        // Least-loaded first, so the roster stays even across the exam season
        List<UserEntity> eligible = userRepository
                .findAllByRole_RoleNameAndIsActiveTrue(RoleEnum.ROLE_TEACHER).stream()
                .filter(user -> !teachingNow.contains(user.getUserId()))
                .filter(user -> invigilatorRepository.findDutyClashes(
                        user.getUserId(), exam.getExamId(), exam.getExamDate(),
                        exam.getStartTime(), exam.getEndTime()).isEmpty())
                .sorted(Comparator.comparingLong(
                        user -> invigilatorRepository.countByUser_UserId(user.getUserId())))
                .toList();

        if (eligible.isEmpty()) {
            warnings.add("No lecturer is free to invigilate at this time, so no duty roster was created.");
            return;
        }

        int assigned = Math.min(needed, eligible.size());
        List<InvigilatorEntity> roster = new ArrayList<>();

        for (int index = 0; index < assigned; index++) {
            roster.add(InvigilatorEntity.builder()
                    .exam(exam)
                    .user(eligible.get(index))
                    // The lightest loaded person takes charge of the sitting
                    .role(index == 0 ? "CHIEF" : "INVIGILATOR")
                    .build());
        }

        invigilatorRepository.saveAll(roster);

        if (assigned < needed) {
            warnings.add(examRooms.size() + " hall(s) and " + candidates + " candidate(s) need "
                    + needed + " invigilator(s), but only " + assigned + " were free.");
        }
    }

    /**
     * First-fit-decreasing bin packing, run once per building so an exam stays in one
     * block when it can. Fewer blocks means fewer invigilators walking between them.
     */
    private List<RoomEntity> chooseHalls(ExamEntity exam, int headcount, List<String> warnings) {
        Map<UUID, Integer> remaining = remainingSeatsByRoom(exam);
        List<RoomEntity> available = availableHalls(exam, remaining);

        if (available.isEmpty()) {
            return List.of();
        }

        // Group the free halls by block, keeping the roomiest first inside each
        Map<String, List<RoomEntity>> byBuilding = new LinkedHashMap<>();
        for (RoomEntity room : available) {
            String key = room.getBuilding() != null
                    ? room.getBuilding().getBuildingName()
                    : "Unassigned";
            byBuilding.computeIfAbsent(key, ignored -> new ArrayList<>()).add(room);
        }

        // The tightest single block that still fits everyone wins
        String bestBuilding = null;
        int bestWaste = Integer.MAX_VALUE;

        for (Map.Entry<String, List<RoomEntity>> entry : byBuilding.entrySet()) {
            int free = seatsAcross(entry.getValue(), remaining);
            if (free >= headcount && free - headcount < bestWaste) {
                bestWaste = free - headcount;
                bestBuilding = entry.getKey();
            }
        }

        if (bestBuilding != null) {
            return packRooms(byBuilding.get(bestBuilding), headcount, remaining);
        }

        // No single block is big enough, so spread across the campus and say so
        int campusSeats = seatsAcross(available, remaining);
        String blocks = String.join(" and ", byBuilding.keySet());

        warnings.add("No single block seats " + headcount + " candidate(s), so the exam is spread across "
                + blocks + ", which offer " + campusSeats + " free exam seat(s) in total.");

        return packRooms(available, headcount, remaining);
    }

    // Take the roomiest halls until everyone has a desk
    private List<RoomEntity> packRooms(List<RoomEntity> rooms, int headcount, Map<UUID, Integer> remaining) {
        List<RoomEntity> sorted = new ArrayList<>(rooms);
        sorted.sort(Comparator.comparingInt(
                (RoomEntity room) -> remaining.getOrDefault(room.getRoomId(), 0)).reversed());

        List<RoomEntity> chosen = new ArrayList<>();
        int covered = 0;

        for (RoomEntity room : sorted) {
            if (covered >= headcount) {
                break;
            }
            chosen.add(room);
            covered += remaining.getOrDefault(room.getRoomId(), 0);
        }

        return chosen;
    }

    private int seatsAcross(List<RoomEntity> rooms, Map<UUID, Integer> remaining) {
        return rooms.stream().mapToInt(room -> remaining.getOrDefault(room.getRoomId(), 0)).sum();
    }

    /**
     * Exam seats each hall can still offer: its spaced capacity less whatever an
     * overlapping exam already holds, so a shared hall is never promised twice.
     */
    private Map<UUID, Integer> remainingSeatsByRoom(ExamEntity exam) {
        Map<UUID, Integer> takenByOtherExams = new HashMap<>();
        for (ExamRoomEntity other : examRoomRepository.findOverlappingAllocations(
                exam.getExamId(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime())) {
            takenByOtherExams.merge(
                    other.getRoom().getRoomId(),
                    other.getAllocatedCapacity() != null ? other.getAllocatedCapacity() : 0,
                    Integer::sum);
        }

        Map<UUID, Integer> remaining = new HashMap<>();
        for (RoomEntity room : roomRepository.findAllByIsAvailableTrueOrderByRoomCodeAsc()) {
            int physical = room.getCapacity() != null ? room.getCapacity() : 0;
            int taken = takenByOtherExams.getOrDefault(room.getRoomId(), 0);

            /*
             * Spacing exists so nobody sits beside someone taking the same paper. A second
             * exam sharing the hall solves that on its own, because it fills the gaps the
             * first one left with a different paper. So one exam alone is held to the spaced
             * capacity, while the hall as a whole may fill right up once it is shared.
             */
            int free = Math.min(physical - taken, examCapacityOf(room));
            remaining.put(room.getRoomId(), Math.max(free, 0));
        }

        return remaining;
    }

    /** Halls with a seat grid that no class occupies and that still have desks free. */
    private List<RoomEntity> availableHalls(ExamEntity exam, Map<UUID, Integer> remaining) {
        Set<UUID> busyWithClasses = new HashSet<>(sessionRepository.findRoomIdsBusyAt(
                exam.getExamDate(), exam.getStartTime(), exam.getEndTime()));

        return roomRepository.findAllByIsAvailableTrueOrderByRoomCodeAsc().stream()
                .filter(room -> room.getSeatRows() != null && room.getSeatsPerRow() != null)
                .filter(room -> !busyWithClasses.contains(room.getRoomId()))
                .filter(room -> remaining.getOrDefault(room.getRoomId(), 0) > 0)
                .toList();
    }

    /**
     * Walks the halls in order, dropping candidates onto desks.
     * Spaced desks are offered first and the in-between ones only once those run out,
     * so a hall shared with another exam ends up interleaved rather than blocked.
     */
    private int seatCandidates(ExamEntity exam, List<RoomEntity> halls, List<StudentEntity> candidates,
                               Map<UUID, Integer> remaining) {
        int cursor = 0;

        for (RoomEntity room : halls) {
            if (cursor >= candidates.size()) {
                break;
            }

            Set<String> alreadyTaken = seatAllocationRepository.findSeatsTakenInRoom(
                            room.getRoomId(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime())
                    .stream()
                    .map(allocation -> allocation.getSeatNumber().toUpperCase())
                    .collect(java.util.stream.Collectors.toSet());

            List<String> order = seatOrder(room).stream()
                    .filter(seat -> !alreadyTaken.contains(seat.toUpperCase()))
                    .toList();

            int freeHere = remaining.getOrDefault(room.getRoomId(), 0);
            int toSeat = Math.min(Math.min(freeHere, candidates.size() - cursor), order.size());

            if (toSeat <= 0) {
                continue;
            }

            ExamRoomEntity examRoom = examRoomRepository.save(ExamRoomEntity.builder()
                    .exam(exam)
                    .room(room)
                    .allocatedCapacity(toSeat)
                    .seatCount(toSeat)
                    .status("ALLOCATED")
                    .build());

            List<SeatAllocationEntity> allocations = new ArrayList<>();
            for (int index = 0; index < toSeat; index++) {
                StudentEntity student = candidates.get(cursor++);
                String seat = order.get(index);

                allocations.add(SeatAllocationEntity.builder()
                        .exam(exam)
                        .examRoom(examRoom)
                        .student(student)
                        .seatNumber(seat)
                        .rowNumber(rowPartOf(seat))
                        .columnNumber(columnPartOf(seat))
                        .build());
            }

            seatAllocationRepository.saveAll(allocations);
        }

        return cursor;
    }

    /**
     * Desk labels in the order they should be filled: every spaced desk first, then the
     * ones deliberately left empty. A1, A3, A5 come before A2, A4, A6 when spacing is 0.5.
     */
    private List<String> seatOrder(RoomEntity room) {
        int rows = room.getSeatRows();
        int columns = room.getSeatsPerRow();
        int capacity = room.getCapacity() != null ? room.getCapacity() : rows * columns;

        double factor = room.getExamCapacityFactor() == null ? 0.5 : room.getExamCapacityFactor();
        int stride = Math.max(1, (int) Math.round(1 / Math.max(factor, 0.01)));

        List<String> spaced = new ArrayList<>();
        List<String> between = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                // The last row of an odd-sized hall is only part filled
                if (row * columns + column >= capacity) {
                    break;
                }

                String seat = rowLetter(row) + (column + 1);
                if (column % stride == 0) {
                    spaced.add(seat);
                } else {
                    between.add(seat);
                }
            }
        }

        List<String> order = new ArrayList<>(spaced);
        order.addAll(between);
        return order;
    }

    // Builds the visual grid for one hall, occupied desks and empty ones alike
    private SeatingRoomDto buildRoomPlan(ExamEntity exam, ExamRoomEntity examRoom) {
        RoomEntity room = examRoom.getRoom();

        Map<String, SeatAllocationEntity> occupants = new HashMap<>();
        for (SeatAllocationEntity allocation : seatAllocationRepository.findSeatsTakenInRoom(
                room.getRoomId(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime())) {
            occupants.put(allocation.getSeatNumber().toUpperCase(), allocation);
        }

        int rows = room.getSeatRows() != null ? room.getSeatRows() : 0;
        int columns = room.getSeatsPerRow() != null ? room.getSeatsPerRow() : 0;
        int capacity = room.getCapacity() != null ? room.getCapacity() : rows * columns;

        List<SeatCellDto> cells = new ArrayList<>();
        int seatedHere = 0;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (row * columns + column >= capacity) {
                    break;
                }

                String seat = rowLetter(row) + (column + 1);
                SeatAllocationEntity allocation = occupants.get(seat.toUpperCase());

                if (allocation == null) {
                    cells.add(SeatCellDto.builder()
                            .seatNumber(seat)
                            .rowLabel(rowLetter(row))
                            .columnNumber(column + 1)
                            .occupied(false)
                            .build());
                    continue;
                }

                boolean mine = allocation.getExam().getExamId().equals(exam.getExamId());
                if (mine) {
                    seatedHere++;
                }

                StudentEntity student = allocation.getStudent();
                cells.add(SeatCellDto.builder()
                        .seatNumber(seat)
                        .rowLabel(rowLetter(row))
                        .columnNumber(column + 1)
                        .occupied(true)
                        .studentId(student.getStudentId())
                        .studentName(student.getUser() != null ? student.getUser().getFullName() : null)
                        .studentNumber(student.getStudentNumber())
                        .moduleCode(allocation.getExam().getModule().getModuleCode())
                        .otherExam(!mine)
                        .build());
            }
        }

        BuildingEntity building = room.getBuilding();

        return SeatingRoomDto.builder()
                .examRoomId(examRoom.getExamRoomId())
                .roomId(room.getRoomId())
                .roomCode(room.getRoomCode())
                .roomName(room.getRoomName())
                .buildingName(building != null ? building.getBuildingName() : null)
                .seatRows(room.getSeatRows())
                .seatsPerRow(room.getSeatsPerRow())
                .capacity(room.getCapacity())
                .examCapacity(examCapacityOf(room))
                .allocatedCapacity(examRoom.getAllocatedCapacity())
                .seatedHere(seatedHere)
                .cells(cells)
                .build();
    }

    // Seats left in a hall once exam spacing is applied
    private int examCapacityOf(RoomEntity room) {
        if (room.getCapacity() == null) {
            return 0;
        }
        double factor = room.getExamCapacityFactor() == null ? 0.5 : room.getExamCapacityFactor();
        return (int) Math.floor(room.getCapacity() * factor);
    }

    // A, B, C ... then AA, AB for very deep halls
    private String rowLetter(int rowIndex) {
        StringBuilder label = new StringBuilder();
        int value = rowIndex;

        do {
            label.insert(0, (char) ('A' + value % 26));
            value = value / 26 - 1;
        } while (value >= 0);

        return label.toString();
    }

    private String rowPartOf(String seat) {
        return seat.replaceAll("[0-9]", "");
    }

    private String columnPartOf(String seat) {
        return seat.replaceAll("[^0-9]", "");
    }

    private ExamEntity findExamOrThrow(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    }
}
