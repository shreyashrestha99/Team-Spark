package com.Backend.Backend.repository;

import com.Backend.Backend.entity.GenerationRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenerationRunRepository extends JpaRepository<GenerationRunEntity, UUID> {

    @Query("SELECT r FROM GenerationRunEntity r WHERE " +
            "(:batchId IS NULL OR r.batch.batchId = :batchId)")
    Page<GenerationRunEntity> searchRuns(@Param("batchId") UUID batchId, Pageable pageable);
}
