package com.Backend.Backend.repository;

import com.Backend.Backend.entity.StudentGroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroupEntity, UUID> {

    List<StudentGroupEntity> findAllByBatch_BatchIdOrderByGroupNameAsc(UUID batchId);

    boolean existsByBatch_BatchIdAndGroupNameIgnoreCase(UUID batchId, String groupName);

    // Uniqueness check that skips the edited row
    boolean existsByBatch_BatchIdAndGroupNameIgnoreCaseAndGroupIdNot(UUID batchId, String groupName, UUID groupId);

    @Query("SELECT g FROM StudentGroupEntity g WHERE " +
            "(:batchId IS NULL OR g.batch.batchId = :batchId)")
    Page<StudentGroupEntity> searchGroups(@Param("batchId") UUID batchId, Pageable pageable);
}
