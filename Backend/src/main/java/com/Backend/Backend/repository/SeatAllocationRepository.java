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
