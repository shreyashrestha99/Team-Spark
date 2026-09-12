package com.Backend.Backend.repository;

import com.Backend.Backend.entity.BatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<BatchEntity, UUID> {

    List<BatchEntity> findAllByIsActiveTrueOrderByBatchNameAsc();

    boolean existsByBatchNameIgnoreCase(String batchName);

    // Uniqueness check that skips the edited row
    boolean existsByBatchNameIgnoreCaseAndBatchIdNot(String batchName, UUID batchId);

    // Combined list, search and programme filter.
    // Search uses "" rather than null so Postgres can type the parameter.
    @Query("SELECT b FROM BatchEntity b WHERE " +
            "(:search = '' OR LOWER(b.batchName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.programme.programmeName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:programmeId IS NULL OR b.programme.programmeId = :programmeId)")
    Page<BatchEntity> searchBatches(
            @Param("search") String search,
            @Param("programmeId") UUID programmeId,
            Pageable pageable
    );
}
