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
     * Finds sessions that overlap the given slot and share the room, teacher or batch.
     * Overlap means start < otherEnd AND end > otherStart. Cancelled sessions are ignored.
     */
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "s.sessionDate = :sessionDate AND " +
            "s.startTime < :endTime AND s.endTime > :startTime AND " +
            "UPPER(s.status) <> 'CANCELLED' AND " +
            "(:excludeId IS NULL OR s.sessionId <> :excludeId) AND " +
            "(s.room.roomId = :roomId OR s.teacher.teacherId = :teacherId OR s.batch.batchId = :batchId)")
    List<TimetableSessionEntity> findClashes(
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("roomId") UUID roomId,
            @Param("teacherId") UUID teacherId,
            @Param("batchId") UUID batchId,
            @Param("excludeId") UUID excludeId
    );

    // Combined timetable list with every filter optional
    @Query("SELECT s FROM TimetableSessionEntity s WHERE " +
            "(:batchId IS NULL OR s.batch.batchId = :batchId) AND " +
            "(:moduleId IS NULL OR s.module.moduleId = :moduleId) AND " +
            "(:teacherId IS NULL OR s.teacher.teacherId = :teacherId) AND " +
            "(:roomId IS NULL OR s.room.roomId = :roomId) AND " +
            "(:fromDate IS NULL OR s.sessionDate >= :fromDate) AND " +
            "(:toDate IS NULL OR s.sessionDate <= :toDate) AND " +
            "(:status IS NULL OR UPPER(s.status) = UPPER(:status))")
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
}
