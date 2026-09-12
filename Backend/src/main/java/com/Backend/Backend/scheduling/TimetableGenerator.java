package com.Backend.Backend.scheduling;

import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Builds a clash-free weekly pattern by constraint satisfaction. No model, no API key,
 * no network: the whole thing is ordinary search over the college's own data.
 *
 * Three phases, which is how timetabling has been solved since long before LLMs.
 *   A. Preprocess  every requirement learns how many legal placements it has.
 *   B. Construct   place hardest first, propagate, undo and retry on a dead end.
 *   C. Improve     nudge sessions around to trim the idle gaps the greedy pass left.
 *
 * The result is guaranteed clash-free because a clash is a hard constraint the search
 * simply cannot express, not a mistake we hope the output avoids.
 */
@Component
@RequiredArgsConstructor
public class TimetableGenerator {

    // Stops a pathological case from searching forever
    private static final int BACKTRACK_BUDGET = 20_000;

    // Only the most promising options of each requirement are explored
    private static final int BRANCHING_LIMIT = 8;

    private static final int OPTIMISE_ITERATIONS = 6_000;

    // Fixed seed so the same inputs always produce the same routine, demos included
    private static final long RANDOM_SEED = 42L;

    private final ScheduleEvaluator evaluator;

    public GenerationOutcome generate(SchedulingContext context, List<SessionRequirement> requirements,
                                      boolean optimise) {
        GenerationOutcome outcome = GenerationOutcome.builder().build();

        if (context.slotCount() == 0) {
            requirements.forEach(requirement -> outcome.getUnplaced().put(requirement,
                    "The weekly grid has no teaching slots. Set up time slots first."));
            return outcome;
        }

        int slotMinutes = slotMinutesOf(context);
        Map<SessionRequirement, Integer> spans = new LinkedHashMap<>();
        for (SessionRequirement requirement : requirements) {
            spans.put(requirement, (int) Math.ceil((double) requirement.getDurationMinutes() / slotMinutes));
        }

        // Phase A: how many placements does each requirement have before anything is placed
        for (SessionRequirement requirement : requirements) {
            requirement.setDomainSize(countLegalPlacements(context, requirement, spans.get(requirement)));
        }

        /*
         * Most constrained first. A workshop needing a 30 seat lab has far fewer options
         * than a tutorial that fits anywhere, so it gets to choose while choices remain.
         * Ties go to the bigger audience, which is the harder one to move later.
         */
        List<SessionRequirement> ordered = new ArrayList<>(requirements);
        ordered.sort(Comparator
                .comparingInt(SessionRequirement::getDomainSize)
                .thenComparing(Comparator.comparingInt(SessionRequirement::getHeadcount).reversed()));

        // Phase B: try for a complete timetable, then settle for the best partial one
        List<PlannedSession> plan = new ArrayList<>();
        int[] backtracks = {0};

        boolean complete = place(context, ordered, 0, spans, plan, backtracks);
        outcome.setBacktracks(backtracks[0]);

        if (!complete) {
            plan.forEach(context::release);
            plan.clear();
            placeGreedily(context, ordered, spans, plan, outcome);
        }

        outcome.setPlan(plan);
        outcome.setPenaltyBeforeOptimise(evaluator.scorePlan(context, plan));
        outcome.setIdleGapsBefore(evaluator.countIdleGaps(context, plan));

        // Phase C
        if (optimise) {
            improve(context, plan, spans);
        }

        outcome.setPenaltyAfterOptimise(evaluator.scorePlan(context, plan));
        outcome.setIdleGapsAfter(evaluator.countIdleGaps(context, plan));

        return outcome;
    }

    /**
     * Depth-first placement with chronological backtracking.
     * Every placement immediately marks its cells busy, so later requirements only ever
     * see options that are still genuinely free. That is the propagation step.
     */
    private boolean place(SchedulingContext context, List<SessionRequirement> ordered, int position,
                          Map<SessionRequirement, Integer> spans, List<PlannedSession> plan, int[] backtracks) {
        if (position == ordered.size()) {
            return true;
        }
        if (backtracks[0] > BACKTRACK_BUDGET) {
            return false;
        }

        SessionRequirement requirement = ordered.get(position);
        int span = spans.get(requirement);
        List<PlannedSession> candidates = rankedCandidates(context, plan, requirement, span);

        int explored = 0;
        for (PlannedSession candidate : candidates) {
            if (explored++ >= BRANCHING_LIMIT) {
                break;
            }

            context.occupy(candidate);
            plan.add(candidate);

            if (place(context, ordered, position + 1, spans, plan, backtracks)) {
                return true;
            }

            // Dead end below us, so undo this choice and try the next best
            plan.remove(plan.size() - 1);
            context.release(candidate);
            backtracks[0]++;
        }

        return false;
    }

    /**
     * Places what it can and records why the rest could not be placed.
     * A partial timetable with clear reasons beats a blank screen and a stack trace.
     */
    private void placeGreedily(SchedulingContext context, List<SessionRequirement> ordered,
                               Map<SessionRequirement, Integer> spans, List<PlannedSession> plan,
                               GenerationOutcome outcome) {
        for (SessionRequirement requirement : ordered) {
            int span = spans.get(requirement);
            List<PlannedSession> candidates = rankedCandidates(context, plan, requirement, span);

            if (candidates.isEmpty()) {
                outcome.getUnplaced().put(requirement, explainFailure(context, requirement, span));
                continue;
            }

            PlannedSession best = candidates.get(0);
            context.occupy(best);
            plan.add(best);
        }
    }

    /**
     * Local search. Repeatedly tries to move one session somewhere better and keeps the
     * move only when the plan's total penalty drops. This is what turns a merely legal
     * timetable into a tidy one with the idle gaps squeezed out.
     */
    private void improve(SchedulingContext context, List<PlannedSession> plan,
                         Map<SessionRequirement, Integer> spans) {
        if (plan.isEmpty()) {
            return;
        }

        Random random = new Random(RANDOM_SEED);
        int best = evaluator.scorePlan(context, plan);

        for (int iteration = 0; iteration < OPTIMISE_ITERATIONS; iteration++) {
            int position = random.nextInt(plan.size());
            PlannedSession current = plan.get(position);
            SessionRequirement requirement = current.getRequirement();
            int span = spans.get(requirement);

            // Free the cells first so the session may also move within its own window
            context.release(current);
            plan.remove(position);

            PlannedSession candidate = bestCandidate(context, plan, requirement, span);
            PlannedSession replacement = candidate == null ? current : candidate;

            context.occupy(replacement);
            plan.add(position, replacement);

            int score = evaluator.scorePlan(context, plan);
            if (score < best) {
                best = score;
                continue;
            }

            // No improvement, so put the session back where it was
            context.release(replacement);
            plan.remove(position);
            context.occupy(current);
            plan.add(position, current);
        }
    }

    /**
     * Every legal placement right now, best scoring first.
     * Each candidate is scored exactly once and the score is carried through the sort,
     * because scoring inside a comparator would re-run it on every comparison.
     */
    private List<PlannedSession> rankedCandidates(SchedulingContext context, List<PlannedSession> plan,
                                                  SessionRequirement requirement, int span) {
        List<ScoredCandidate> scored = new ArrayList<>();

        forEachLegalPlacement(context, requirement, span, candidate ->
                scored.add(new ScoredCandidate(candidate, evaluator.scoreCandidate(
                        context, plan, requirement,
                        candidate.getStartSlotIndex(), span, candidate.getRoom()))));

        scored.sort(Comparator.comparingInt(ScoredCandidate::score));

        List<PlannedSession> candidates = new ArrayList<>(scored.size());
        scored.forEach(entry -> candidates.add(entry.candidate()));
        return candidates;
    }

    /**
     * The single best placement, found without sorting.
     * The optimiser only ever wants the winner, so ranking the other few hundred
     * options each iteration would be wasted work.
     */
    private PlannedSession bestCandidate(SchedulingContext context, List<PlannedSession> plan,
                                         SessionRequirement requirement, int span) {
        ScoredCandidate[] best = {null};

        forEachLegalPlacement(context, requirement, span, candidate -> {
            int score = evaluator.scoreCandidate(
                    context, plan, requirement, candidate.getStartSlotIndex(), span, candidate.getRoom());

            if (best[0] == null || score < best[0].score()) {
                best[0] = new ScoredCandidate(candidate, score);
            }
        });

        return best[0] == null ? null : best[0].candidate();
    }

    // Walks every room and start slot that would be a legal home for this requirement
    private void forEachLegalPlacement(SchedulingContext context, SessionRequirement requirement, int span,
                                       Consumer<PlannedSession> consumer) {
        for (RoomEntity room : context.getRooms()) {
            if (!evaluator.roomSuits(requirement, room)) {
                continue;
            }

            for (int startIndex = 0; startIndex + span <= context.slotCount(); startIndex++) {
                if (!evaluator.isLegal(context, requirement, startIndex, span, room)) {
                    continue;
                }

                consumer.accept(PlannedSession.builder()
                        .requirement(requirement)
                        .startSlotIndex(startIndex)
                        .slotSpan(span)
                        .room(room)
                        .build());
            }
        }
    }

    // A placement paired with the score it earned, so the sort never rescores
    private record ScoredCandidate(PlannedSession candidate, int score) {
    }

    // Ignores who is busy, so it measures how tight a requirement is in principle
    private int countLegalPlacements(SchedulingContext context, SessionRequirement requirement, int span) {
        int count = 0;

        for (RoomEntity room : context.getRooms()) {
            if (!evaluator.roomSuits(requirement, room)) {
                continue;
            }
            for (int startIndex = 0; startIndex + span <= context.slotCount(); startIndex++) {
                if (context.isContiguousRun(startIndex, span)
                        && context.teacherAvailable(startIndex, requirement.getTeacher().getTeacherId())) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Works out which resource actually ran out, so the admin is told the cause
     * rather than just the word "conflict".
     */
    private String explainFailure(SchedulingContext context, SessionRequirement requirement, int span) {
        long suitableRooms = context.getRooms().stream()
                .filter(room -> evaluator.roomSuits(requirement, room))
                .count();

        if (suitableRooms == 0) {
            String kit = requirement.isNeedsComputers() ? " with computers" : "";
            return "No room" + kit + " seats " + requirement.getHeadcount()
                    + ". Add a bigger room or split the group.";
        }

        long availableSlots = 0;
        for (int startIndex = 0; startIndex + span <= context.slotCount(); startIndex++) {
            if (context.isContiguousRun(startIndex, span)
                    && context.teacherAvailable(startIndex, requirement.getTeacher().getTeacherId())) {
                availableSlots++;
            }
        }

        String teacherName = requirement.getTeacher().getUser() != null
                ? requirement.getTeacher().getUser().getFullName()
                : "The lecturer";

        if (availableSlots == 0) {
            return teacherName + " has no free window long enough for a "
                    + requirement.getDurationMinutes() + " minute session.";
        }

        return "All " + suitableRooms + " suitable room(s) are already booked in every slot "
                + teacherName + " and this group are both free.";
    }

    // The grid is uniform, so one slot's length sizes every multi-period session
    private int slotMinutesOf(SchedulingContext context) {
        TimeSlotEntity first = context.slotAt(0);
        long minutes = Duration.between(first.getStartTime(), first.getEndTime()).toMinutes();
        return minutes > 0 ? (int) minutes : 60;
    }
}
