package com.Backend.Backend.repository;

import com.Backend.Backend.entity.ExamRoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRoomRepository extends JpaRepository<ExamRoomEntity, UUID> {

    // Blocks allocating the same room twice
    boolean existsByExam_ExamIdAndRoom_RoomId(UUID examId, UUID roomId);

    List<ExamRoomEntity> findAllByExam_ExamId(UUID examId);

    /**
     * Finds the same room allocated to another exam whose slot overlaps.
     * Prevents one hall being used by two exams at once.
     */
    @Query("SELECT er FROM ExamRoomEntity er WHERE " +
            "er.room.roomId = :roomId AND " +
            "er.exam.examDate = :examDate AND " +
            "er.exam.startTime < :endTime AND er.exam.endTime > :startTime AND " +
            "er.exam.examId <> :examId AND " +
            "UPPER(er.exam.status) <> 'CANCELLED'")
    List<ExamRoomEntity> findRoomClashes(
            @Param("roomId") UUID roomId,
            @Param("examId") UUID examId,
            @Param("examDate") java.time.LocalDate examDate,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime
    );

    // Combined list with exam and room filters
    @Query("SELECT er FROM ExamRoomEntity er WHERE " +
            "(:examId IS NULL OR er.exam.examId = :examId) AND " +
            "(:roomId IS NULL OR er.room.roomId = :roomId)")
    Page<ExamRoomEntity> searchExamRooms(
            @Param("examId") UUID examId,
            @Param("roomId") UUID roomId,
            Pageable pageable
    );
}
