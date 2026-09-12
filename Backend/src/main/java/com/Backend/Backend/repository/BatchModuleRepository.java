package com.Backend.Backend.repository;

import com.Backend.Backend.entity.BatchModuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchModuleRepository extends JpaRepository<BatchModuleEntity, UUID> {

    // Blocks assigning the same module twice
    boolean existsByBatch_BatchIdAndModule_ModuleId(UUID batchId, UUID moduleId);

    // Delivery patterns the generator expands into session requirements
    List<BatchModuleEntity> findAllByBatch_BatchId(UUID batchId);

    // Combined list with batch and module filters
    @Query("SELECT bm FROM BatchModuleEntity bm WHERE " +
            "(:batchId IS NULL OR bm.batch.batchId = :batchId) AND " +
            "(:moduleId IS NULL OR bm.module.moduleId = :moduleId)")
    Page<BatchModuleEntity> searchBatchModules(
            @Param("batchId") UUID batchId,
            @Param("moduleId") UUID moduleId,
            Pageable pageable
    );
}
