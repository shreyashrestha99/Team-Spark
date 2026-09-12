package com.Backend.Backend.repository;

import com.Backend.Backend.entity.SeatAllocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocationEntity, UUID> {

    // One seat per student per exam
    boolean existsByExam_ExamIdAndStudent_StudentId(UUID examId, UUID studentId);

    // One student per seat in a room
    boolean existsByExamRoom_ExamRoomIdAndSeatNumberIgnoreCase(UUID examRoomId, String seatNumber);

    long countByExamRoom_ExamRoomId(UUID examRoomId);

    List<SeatAllocationEntity> findAllByExam_ExamId(UUID examId);

    // Every desk one student has been given, earliest exam first
    @Query("SELECT sa FROM SeatAllocationEntity sa " +
            "JOIN FETCH sa.exam e JOIN FETCH e.module JOIN FETCH e.batch " +
            "JOIN FETCH sa.examRoom er JOIN FETCH er.room r LEFT JOIN FETCH r.building " +
            "WHERE sa.student.studentId = :studentId AND UPPER(e.status) <> 'CANCELLED' " +
            "ORDER BY e.examDate ASC, e.startTime ASC")
    List<SeatAllocationEntity> findMySeats(@Param("studentId") UUID studentId);

    void deleteAllByExam_ExamId(UUID examId);

    /**
     * Seats already claimed in one physical hall during an overlapping slot, whichever
     * exam claimed them. Two exams may share a hall, so seat labels must be unique per
     * room and time rather than per exam.
     */
    @Query("SELECT sa FROM SeatAllocationEntity sa WHERE " +
            "sa.examRoom.room.roomId = :roomId AND " +
            "sa.exam.examDate = :examDate AND " +
            "sa.exam.startTime < :endTime AND sa.exam.endTime > :startTime AND " +
            "UPPER(sa.exam.status) <> 'CANCELLED'")
    List<SeatAllocationEntity> findSeatsTakenInRoom(
            @Param("roomId") UUID roomId,
            @Param("examDate") java.time.LocalDate examDate,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime
    );

    // Combined list with exam, room and student filters
    @Query("SELECT sa FROM SeatAllocationEntity sa WHERE " +
            "(:examId IS NULL OR sa.exam.examId = :examId) AND " +
            "(:examRoomId IS NULL OR sa.examRoom.examRoomId = :examRoomId) AND " +
            "(:studentId IS NULL OR sa.student.studentId = :studentId)")
    Page<SeatAllocationEntity> searchAllocations(
            @Param("examId") UUID examId,
            @Param("examRoomId") UUID examRoomId,
            @Param("studentId") UUID studentId,
            Pageable pageable
    );
}
