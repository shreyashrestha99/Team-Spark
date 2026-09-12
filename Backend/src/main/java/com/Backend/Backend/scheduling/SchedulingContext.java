package com.Backend.Backend.scheduling;

import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.TeacherAvailabilityEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.TimetableSessionEntity;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everything the generator needs in memory: the weekly grid, the rooms it may use,
 * who is already busy in each cell, and the windows each lecturer has declared.
 *
 * Occupancy starts out holding sessions other batches already own, then grows and
 * shrinks as the search places and unplaces its own sessions.
 */
@Getter
public class SchedulingContext {

    private final List<TimeSlotEntity> slots;
    private final List<RoomEntity> rooms;

    // Busy sets, one entry per slot in the grid
    private final List<Set<UUID>> busyRooms = new ArrayList<>();
    private final List<Set<UUID>> busyTeachers = new ArrayList<>();
    private final List<Set<UUID>> busyBatches = new ArrayList<>();
    private final List<Set<UUID>> busyGroups = new ArrayList<>();

    private final Map<UUID, List<TeacherAvailabilityEntity>> unavailableWindows = new HashMap<>();
    private final Map<UUID, List<TeacherAvailabilityEntity>> preferredWindows = new HashMap<>();

    // Windows folded into slot-indexed masks, built on first use and reused after
    private final Map<UUID, boolean[]> unavailableMasks = new HashMap<>();
    private final Map<UUID, boolean[]> preferredMasks = new HashMap<>();

    // Groups belonging to each batch, so a lecture can block every one of them
    private final Map<UUID, Set<UUID>> groupsByBatch = new HashMap<>();

    public SchedulingContext(
            List<TimeSlotEntity> slots,
            List<RoomEntity> rooms,
            List<TeacherAvailabilityEntity> availability,
            Map<UUID, Set<UUID>> groupsByBatch
    ) {
        this.slots = slots;
        this.rooms = rooms;

        for (int index = 0; index < slots.size(); index++) {
            busyRooms.add(new HashSet<>());
            busyTeachers.add(new HashSet<>());
            busyBatches.add(new HashSet<>());
            busyGroups.add(new HashSet<>());
        }

        for (TeacherAvailabilityEntity window : availability) {
            UUID teacherId = window.getTeacher().getTeacherId();
            Map<UUID, List<TeacherAvailabilityEntity>> target =
                    "PREFERRED".equalsIgnoreCase(window.getAvailabilityType())
                            ? preferredWindows
                            : unavailableWindows;
            target.computeIfAbsent(teacherId, key -> new ArrayList<>()).add(window);
        }

        this.groupsByBatch.putAll(groupsByBatch);
    }

    public int slotCount() {
        return slots.size();
    }

    public TimeSlotEntity slotAt(int index) {
        return slots.get(index);
    }

    /**
     * Projects a session that already exists onto the grid, marking every cell it overlaps.
     * A session from another batch is treated exactly like one this run placed itself.
     */
    public void occupyExisting(TimetableSessionEntity session) {
        DayOfWeek day = session.getSessionDate().getDayOfWeek();

        for (int index = 0; index < slots.size(); index++) {
            TimeSlotEntity slot = slots.get(index);
            if (slot.getDayOfWeek() != day) {
                continue;
            }
            if (!overlaps(slot.getStartTime(), slot.getEndTime(),
                    session.getStartTime(), session.getEndTime())) {
                continue;
            }

            busyRooms.get(index).add(session.getRoom().getRoomId());
            busyTeachers.get(index).add(session.getTeacher().getTeacherId());

            if (session.getStudentGroup() == null) {
                busyBatches.get(index).add(session.getBatch().getBatchId());
            } else {
                busyGroups.get(index).add(session.getStudentGroup().getGroupId());
            }
        }
    }

    // Marks the cells a planned session covers
    public void occupy(PlannedSession planned) {
        applyOccupancy(planned, true);
    }

    // Frees the cells again when the search backtracks
    public void release(PlannedSession planned) {
        applyOccupancy(planned, false);
    }

    private void applyOccupancy(PlannedSession planned, boolean add) {
        SessionRequirement requirement = planned.getRequirement();
        UUID roomId = planned.getRoom().getRoomId();
        UUID teacherId = requirement.getTeacher().getTeacherId();
        UUID batchId = requirement.getBatch().getBatchId();
        UUID groupId = requirement.getStudentGroup() != null
                ? requirement.getStudentGroup().getGroupId()
                : null;

        for (int index = planned.getStartSlotIndex(); index < planned.endSlotIndexExclusive(); index++) {
            if (add) {
                busyRooms.get(index).add(roomId);
                busyTeachers.get(index).add(teacherId);
                if (groupId == null) {
                    busyBatches.get(index).add(batchId);
                } else {
                    busyGroups.get(index).add(groupId);
                }
            } else {
                busyRooms.get(index).remove(roomId);
                busyTeachers.get(index).remove(teacherId);
                if (groupId == null) {
                    busyBatches.get(index).remove(batchId);
                } else {
                    busyGroups.get(index).remove(groupId);
                }
            }
        }
    }

    /**
     * Whether the audience is free. A lecture collides with every group of its batch,
     * and a group session collides with any lecture that batch is already sitting in.
     */
    public boolean audienceFree(int slotIndex, UUID batchId, UUID groupId) {
        if (busyBatches.get(slotIndex).contains(batchId)) {
            return false;
        }

        if (groupId == null) {
            Set<UUID> groups = groupsByBatch.getOrDefault(batchId, Set.of());
            return groups.stream().noneMatch(busyGroups.get(slotIndex)::contains);
        }

        return !busyGroups.get(slotIndex).contains(groupId);
    }

    public boolean roomFree(int slotIndex, UUID roomId) {
        return !busyRooms.get(slotIndex).contains(roomId);
    }

    public boolean teacherFree(int slotIndex, UUID teacherId) {
        return !busyTeachers.get(slotIndex).contains(teacherId);
    }

    /**
     * A declared UNAVAILABLE window is a hard block.
     *
     * The search asks this millions of times, so each lecturer's windows are folded once
     * into a slot-indexed mask. Scanning the window list here instead would rebuild the
     * same answer on every legality check.
     */
    public boolean teacherAvailable(int slotIndex, UUID teacherId) {
        return !maskFor(unavailableMasks, unavailableWindows, teacherId)[slotIndex];
    }

    // A PREFERRED window only earns the placement a discount
    public boolean teacherPrefers(int slotIndex, UUID teacherId) {
        return maskFor(preferredMasks, preferredWindows, teacherId)[slotIndex];
    }

    // Folds one lecturer's windows into a slot-indexed mask the first time it is needed
    private boolean[] maskFor(Map<UUID, boolean[]> cache,
                              Map<UUID, List<TeacherAvailabilityEntity>> windowsByTeacher,
                              UUID teacherId) {
        return cache.computeIfAbsent(teacherId, key -> {
            boolean[] mask = new boolean[slots.size()];
            List<TeacherAvailabilityEntity> windows = windowsByTeacher.getOrDefault(key, List.of());

            for (int index = 0; index < slots.size(); index++) {
                TimeSlotEntity slot = slots.get(index);
                for (TeacherAvailabilityEntity window : windows) {
                    if (window.getDayOfWeek() == slot.getDayOfWeek()
                            && overlaps(slot.getStartTime(), slot.getEndTime(),
                            window.getStartTime(), window.getEndTime())) {
                        mask[index] = true;
                        break;
                    }
                }
            }

            return mask;
        });
    }

    /**
     * Whether slots start..start+span-1 form one unbroken block on a single day.
     * A two-hour session cannot straddle a lunch break or roll over into the next day.
     */
    public boolean isContiguousRun(int startIndex, int span) {
        if (startIndex < 0 || startIndex + span > slots.size()) {
            return false;
        }

        TimeSlotEntity first = slots.get(startIndex);
        for (int offset = 1; offset < span; offset++) {
            TimeSlotEntity previous = slots.get(startIndex + offset - 1);
            TimeSlotEntity current = slots.get(startIndex + offset);

            if (current.getDayOfWeek() != first.getDayOfWeek()) {
                return false;
            }
            if (!previous.getEndTime().equals(current.getStartTime())) {
                return false;
            }
        }

        return true;
    }

    private static boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
