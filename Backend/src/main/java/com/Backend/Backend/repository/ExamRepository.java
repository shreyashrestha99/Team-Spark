package com.Backend.Backend.repository;

import com.Backend.Backend.entity.ExamEntity;
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
public interface ExamRepository extends JpaRepository<ExamEntity, UUID> {

    // Overlapping exams for the same batch
    @Query("SELECT e FROM ExamEntity e WHERE " +
            "e.batch.batchId = :batchId AND " +
            "e.examDate = :examDate AND " +
            "e.startTime < :endTime AND e.endTime > :startTime AND " +
            "UPPER(e.status) <> 'CANCELLED' AND " +
            "(:excludeId IS NULL OR e.examId <> :excludeId)")
    List<ExamEntity> findBatchClashes(
            @Param("batchId") UUID batchId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") UUID excludeId
    );

    // Blocks scheduling the same module twice for a batch
    boolean existsByBatch_BatchIdAndModule_ModuleId(UUID batchId, UUID moduleId);

    // Combined exam list with every filter optional.
    // Status uses "" rather than null so Postgres can type the parameter.
    @Query("SELECT e FROM ExamEntity e WHERE " +
            "(:batchId IS NULL OR e.batch.batchId = :batchId) AND " +
            "(:moduleId IS NULL OR e.module.moduleId = :moduleId) AND " +
            "(:fromDate IS NULL OR e.examDate >= :fromDate) AND " +
            "(:toDate IS NULL OR e.examDate <= :toDate) AND " +
            "(:status = '' OR UPPER(e.status) = UPPER(:status))")
    Page<ExamEntity> searchExams(
            @Param("batchId") UUID batchId,
            @Param("moduleId") UUID moduleId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            Pageable pageable
    );
}
