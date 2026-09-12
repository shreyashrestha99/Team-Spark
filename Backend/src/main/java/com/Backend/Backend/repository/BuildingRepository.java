package com.Backend.Backend.repository;

import com.Backend.Backend.entity.BuildingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, UUID> {

    Optional<BuildingEntity> findByBuildingCodeIgnoreCase(String buildingCode);

    boolean existsByBuildingCodeIgnoreCase(String buildingCode);

    // Uniqueness check that skips the edited row
    boolean existsByBuildingCodeIgnoreCaseAndBuildingIdNot(String buildingCode, UUID buildingId);

    boolean existsByBuildingNameIgnoreCase(String buildingName);

    boolean existsByBuildingNameIgnoreCaseAndBuildingIdNot(String buildingName, UUID buildingId);

    List<BuildingEntity> findAllByIsActiveTrueOrderByBuildingNameAsc();

    @Query("SELECT b FROM BuildingEntity b WHERE " +
            "LOWER(b.buildingName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.buildingCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<BuildingEntity> searchBuildings(@Param("search") String search, Pageable pageable);
}
