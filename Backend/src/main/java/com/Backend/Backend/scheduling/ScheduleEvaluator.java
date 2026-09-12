package com.Backend.Backend.scheduling;

import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The single place that decides whether a placement is legal and how good it is.
 *
 * Generation, the optimiser and any validation all call this, so there is only ever
 * one definition of a clash and one definition of a tidy timetable.
 *
 * Hard rules are absolute: break one and the timetable is invalid. Soft rules are
 * weighted penalties, and a lower total means a better timetable.
 */
@Component
public class ScheduleEvaluator {

    // Soft weights. Raise one and the optimiser works harder on that goal.
    private static final int PENALTY_STUDENT_IDLE_SLOT = 40;
    private static final int PENALTY_SAME_MODULE_SAME_DAY = 25;
    private static final int PENALTY_TEACHER_IDLE_SLOT = 10;
    private static final int PENALTY_PER_WASTED_SEAT = 1;
    private static final int PENALTY_LATE_PERIOD = 6;
    private static final int PENALTY_TUTORIAL_BEFORE_LECTURE = 30;
    private static final int PENALTY_BUILDING_SWITCH = 15;
    private static final int BONUS_TEACHER_PREFERRED = 20;

    /**
     * Every hard rule for one candidate placement.
     * H1 room free, H2 lecturer free, H3 audience free, H4 capacity, H5 room type,
     * H6 lecturer available, H7 the block is unbroken and on one day.
     */
    public boolean isLegal(SchedulingContext context, SessionRequirement requirement, int startIndex, int span,
                           RoomEntity room) {
        if (!context.isContiguousRun(startIndex, span)) {
            return false;
        }
        if (!roomSuits(requirement, room)) {
            return false;
        }

        UUID roomId = room.getRoomId();
        UUID teacherId = requirement.getTeacher().getTeacherId();
        UUID batchId = requirement.getBatch().getBatchId();
        UUID groupId = requirement.getStudentGroup() != null
                ? requirement.getStudentGroup().getGroupId()
                : null;

        for (int index = startIndex; index < startIndex + span; index++) {
            if (!context.roomFree(index, roomId)) return false;
            if (!context.teacherFree(index, teacherId)) return false;
            if (!context.audienceFree(index, batchId, groupId)) return false;
            if (!context.teacherAvailable(index, teacherId)) return false;
        }

        return true;
    }

    /** H4 and H5: the room has to hold the audience and carry the kit the session needs. */
    public boolean roomSuits(SessionRequirement requirement, RoomEntity room) {
        if (!Boolean.TRUE.equals(room.getIsAvailable())) {
            return false;
        }
        if (room.getCapacity() == null || room.getCapacity() < requirement.getHeadcount()) {
            return false;
        }
        return !requirement.isNeedsComputers() || Boolean.TRUE.equals(room.getHasComputers());
    }

    /**
     * Scores one candidate against what is already planned. Cheap enough to run for
     * every option of every requirement, which is what makes the greedy pass sensible.
     */
    public int scoreCandidate(SchedulingContext context, List<PlannedSession> plan,
                              SessionRequirement requirement, int startIndex, int span, RoomEntity room) {
        int penalty = 0;
        TimeSlotEntity startSlot = context.slotAt(startIndex);

        // Wasting a big hall on a small group blocks it for a cohort that needs it
        penalty += (room.getCapacity() - requirement.getHeadcount()) * PENALTY_PER_WASTED_SEAT;

        // Late periods suit nobody, so drift towards the start of the day
        penalty += startSlot.getPeriodNumber() * PENALTY_LATE_PERIOD;

        if (context.teacherPrefers(startIndex, requirement.getTeacher().getTeacherId())) {
            penalty -= BONUS_TEACHER_PREFERRED;
        }

        for (PlannedSession placed : plan) {
            SessionRequirement other = placed.getRequirement();
            TimeSlotEntity otherSlot = context.slotAt(placed.getStartSlotIndex());
            boolean sameDay = otherSlot.getDayOfWeek() == startSlot.getDayOfWeek();

            // Spread a module across the week rather than stacking it on one day
            if (sameDay && other.getModule().getModuleId().equals(requirement.getModule().getModuleId())) {
                penalty += PENALTY_SAME_MODULE_SAME_DAY;
            }

            // A tutorial or workshop should follow its lecture, not lead it
            if (other.getModule().getModuleId().equals(requirement.getModule().getModuleId())) {
                penalty += lectureOrderPenalty(requirement, startIndex, other, placed.getStartSlotIndex());
            }

            if (!sameDay) {
                continue;
            }

            // Idle hours between a student's classes are the worst part of a bad routine
            if (sharesAudience(requirement, other)) {
                penalty += gapBetween(placed, startIndex, span) * PENALTY_STUDENT_IDLE_SLOT;
                penalty += buildingSwitchPenalty(context, placed, startIndex, span, room);
            }

            if (other.getTeacher().getTeacherId().equals(requirement.getTeacher().getTeacherId())) {
                penalty += gapBetween(placed, startIndex, span) * PENALTY_TEACHER_IDLE_SLOT;
            }
        }

        return penalty;
    }

    /** Total penalty of a finished plan, the number the optimiser drives down. */
    public int scorePlan(SchedulingContext context, List<PlannedSession> plan) {
        int penalty = 0;

        for (int index = 0; index < plan.size(); index++) {
            PlannedSession planned = plan.get(index);
            List<PlannedSession> earlier = plan.subList(0, index);

            penalty += scoreCandidate(
                    context,
                    earlier,
                    planned.getRequirement(),
                    planned.getStartSlotIndex(),
                    planned.getSlotSpan(),
                    planned.getRoom()
            );
        }

        return penalty;
    }

    /** Idle slots a student sits through in one week, reported next to the score. */
    public int countIdleGaps(SchedulingContext context, List<PlannedSession> plan) {
        Map<String, List<PlannedSession>> byAudienceAndDay = new HashMap<>();

        for (PlannedSession planned : plan) {
            SessionRequirement requirement = planned.getRequirement();
            String audience = requirement.getStudentGroup() != null
                    ? requirement.getStudentGroup().getGroupId().toString()
                    : requirement.getBatch().getBatchId().toString();
            String key = audience + "|" + context.slotAt(planned.getStartSlotIndex()).getDayOfWeek();
            byAudienceAndDay.computeIfAbsent(key, ignored -> new ArrayList<>()).add(planned);
        }

        int gaps = 0;
        for (List<PlannedSession> sameDay : byAudienceAndDay.values()) {
            sameDay.sort((left, right) -> Integer.compare(left.getStartSlotIndex(), right.getStartSlotIndex()));

            for (int index = 1; index < sameDay.size(); index++) {
                int previousEnd = sameDay.get(index - 1).endSlotIndexExclusive();
                int currentStart = sameDay.get(index).getStartSlotIndex();
                gaps += Math.max(0, currentStart - previousEnd);
            }
        }

        return gaps;
    }

    // Whether the same students sit in both sessions
    private boolean sharesAudience(SessionRequirement left, SessionRequirement right) {
        if (!left.getBatch().getBatchId().equals(right.getBatch().getBatchId())) {
            return false;
        }
        if (left.getStudentGroup() == null || right.getStudentGroup() == null) {
            return true;
        }
        return left.getStudentGroup().getGroupId().equals(right.getStudentGroup().getGroupId());
    }

    // You learn it in the lecture, then practise it in the tutorial or workshop
    private int lectureOrderPenalty(SessionRequirement candidate, int candidateStart,
                                    SessionRequirement other, int otherStart) {
        boolean candidateIsLecture = "LECTURE".equals(candidate.getSessionType());
        boolean otherIsLecture = "LECTURE".equals(other.getSessionType());

        if (candidateIsLecture == otherIsLecture) {
            return 0;
        }
        if (candidateIsLecture) {
            return candidateStart > otherStart ? PENALTY_TUTORIAL_BEFORE_LECTURE : 0;
        }
        return candidateStart < otherStart ? PENALTY_TUTORIAL_BEFORE_LECTURE : 0;
    }

    // Back-to-back classes in different blocks mean a walk across campus
    private int buildingSwitchPenalty(SchedulingContext context, PlannedSession placed,
                                      int startIndex, int span, RoomEntity room) {
        boolean adjacent = placed.endSlotIndexExclusive() == startIndex
                || startIndex + span == placed.getStartSlotIndex();

        if (!adjacent) {
            return 0;
        }

        UUID placedBuilding = placed.getRoom().getBuilding() != null
                ? placed.getRoom().getBuilding().getBuildingId()
                : null;
        UUID candidateBuilding = room.getBuilding() != null
                ? room.getBuilding().getBuildingId()
                : null;

        if (placedBuilding == null || candidateBuilding == null) {
            return 0;
        }

        return placedBuilding.equals(candidateBuilding) ? 0 : PENALTY_BUILDING_SWITCH;
    }

    // Empty slots between the candidate and an already placed session
    private int gapBetween(PlannedSession placed, int startIndex, int span) {
        if (startIndex >= placed.endSlotIndexExclusive()) {
            return startIndex - placed.endSlotIndexExclusive();
        }
        if (placed.getStartSlotIndex() >= startIndex + span) {
            return placed.getStartSlotIndex() - (startIndex + span);
        }
        return 0;
    }
}
