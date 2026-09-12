package com.Backend.Backend.repository;

import com.Backend.Backend.entity.TimetableSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableSessionRepository extends JpaRepository<TimetableSessionEntity, UUID> {

    /**
     * Finds sessions that overlap the given slot and share the room, teacher or student audience.
     * Overlap means start < otherEnd AND end > otherStart. Cancelled sessions are ignored.
     *
     * A student clash needs the group rules: a whole-batch lecture (null group) collides with every
     * group of that batch, while two group sessions only collide when it is the same group.
     */
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "s.sessionDate = :sessionDate AND " +
            "s.startTime < :endTime AND s.endTime > :startTime AND " +
            "UPPER(s.status) <> 'CANCELLED' AND " +
            "(:excludeId IS NULL OR s.sessionId <> :excludeId) AND " +
            "(s.room.roomId = :roomId OR s.teacher.teacherId = :teacherId OR " +
            "(s.batch.batchId = :batchId AND (s.studentGroup IS NULL OR :groupId IS NULL " +
            "OR s.studentGroup.groupId = :groupId)))")
    List<TimetableSessionEntity> findClashes(
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("roomId") UUID roomId,
            @Param("teacherId") UUID teacherId,
            @Param("batchId") UUID batchId,
            @Param("groupId") UUID groupId,
            @Param("excludeId") UUID excludeId
    );

    // Combined timetable list with every filter optional.
    // Status uses "" rather than null so Postgres can type the parameter.
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "(:batchId IS NULL OR s.batch.batchId = :batchId) AND " +
            "(:moduleId IS NULL OR s.module.moduleId = :moduleId) AND " +
            "(:teacherId IS NULL OR s.teacher.teacherId = :teacherId) AND " +
            "(:roomId IS NULL OR s.room.roomId = :roomId) AND " +
            "(:fromDate IS NULL OR s.sessionDate >= :fromDate) AND " +
            "(:toDate IS NULL OR s.sessionDate <= :toDate) AND " +
            "(:status = '' OR UPPER(s.status) = UPPER(:status))")
    Page<TimetableSessionEntity> searchSessions(
            @Param("batchId") UUID batchId,
            @Param("moduleId") UUID moduleId,
            @Param("teacherId") UUID teacherId,
            @Param("roomId") UUID roomId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            Pageable pageable
    );

    // Weekly contact minutes for workload tracking
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "s.teacher.teacherId = :teacherId AND " +
            "s.sessionDate BETWEEN :fromDate AND :toDate AND " +
            "UPPER(s.status) <> 'CANCELLED'")
    List<TimetableSessionEntity> findTeacherSessionsBetween(
            @Param("teacherId") UUID teacherId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Everything already on the calendar in a window, eagerly joined.
     * The generator projects these onto the weekly grid to learn what is already taken.
     */
    @Query("SELECT s FROM TimetableSessionEntity s " +
            "JOIN FETCH s.room JOIN FETCH s.teacher JOIN FETCH s.batch JOIN FETCH s.module " +
            "LEFT JOIN FETCH s.studentGroup " +
            "WHERE s.sessionDate BETWEEN :fromDate AND :toDate AND UPPER(s.status) <> 'CANCELLED'")
    List<TimetableSessionEntity> findAllInWindow(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // Rooms tied up by a class at that moment, so an exam is never put in one
    @Query("SELECT DISTINCT s.room.roomId FROM TimetableSessionEntity s WHERE " +
            "s.sessionDate = :date AND " +
            "s.startTime < :endTime AND s.endTime > :startTime AND " +
            "UPPER(s.status) <> 'CANCELLED'")
    List<UUID> findRoomIdsBusyAt(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Sessions produced by one generator run, used to roll a routine back
    List<TimetableSessionEntity> findAllByGenerationRun_RunId(UUID runId);

    void deleteAllByGenerationRun_RunId(UUID runId);

    // Clears a batch's existing routine before regenerating it
    void deleteAllByBatch_BatchIdAndSessionDateBetween(UUID batchId, LocalDate fromDate, LocalDate toDate);

    // Booked minutes per room, the numerator of the utilisation figure
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "s.sessionDate BETWEEN :fromDate AND :toDate AND UPPER(s.status) <> 'CANCELLED'")
    List<TimetableSessionEntity> findAllForUtilisation(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
