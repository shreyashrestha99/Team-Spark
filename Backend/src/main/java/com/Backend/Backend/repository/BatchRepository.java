package com.Backend.Backend.repository;

import com.Backend.Backend.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<BatchEntity, UUID> {

    List<BatchEntity> findAllByIsActiveTrueOrderByBatchNameAsc();

    boolean existsByBatchNameIgnoreCase(String batchName);
}
