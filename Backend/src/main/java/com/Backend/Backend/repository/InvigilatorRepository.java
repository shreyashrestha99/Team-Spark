package com.Backend.Backend.repository;

import com.Backend.Backend.entity.InvigilatorEntity;
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
public interface InvigilatorRepository extends JpaRepository<InvigilatorEntity, UUID> {

    // Blocks assigning the same person twice
    boolean existsByExam_ExamIdAndUser_UserId(UUID examId, UUID userId);

    List<InvigilatorEntity> findAllByExam_ExamId(UUID examId);

    long countByUser_UserId(UUID userId);

    void deleteAllByExam_ExamId(UUID examId);

    // One person's invigilation roster, earliest duty first
    @Query("SELECT i FROM InvigilatorEntity i " +
            "JOIN FETCH i.exam e JOIN FETCH e.module JOIN FETCH e.batch " +
            "WHERE i.user.userId = :userId AND UPPER(e.status) <> 'CANCELLED' " +
            "ORDER BY e.examDate ASC, e.startTime ASC")
    List<InvigilatorEntity> findMyDuties(@Param("userId") UUID userId);

    /**
     * Finds other exams where this person is already on duty at an overlapping time.
     * Keeps one invigilator from being booked into two halls at once.
     */
    @Query("SELECT i FROM InvigilatorEntity i WHERE " +
            "i.user.userId = :userId AND " +
            "i.exam.examDate = :examDate AND " +
            "i.exam.startTime < :endTime AND i.exam.endTime > :startTime AND " +
            "i.exam.examId <> :examId AND " +
            "UPPER(i.exam.status) <> 'CANCELLED'")
    List<InvigilatorEntity> findDutyClashes(
            @Param("userId") UUID userId,
            @Param("examId") UUID examId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Combined list with exam and user filters
    @Query("SELECT i FROM InvigilatorEntity i WHERE " +
            "(:examId IS NULL OR i.exam.examId = :examId) AND " +
            "(:userId IS NULL OR i.user.userId = :userId)")
    Page<InvigilatorEntity> searchInvigilators(
            @Param("examId") UUID examId,
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
