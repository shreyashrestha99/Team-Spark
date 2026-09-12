package com.Backend.Backend.repository;

import com.Backend.Backend.entity.ProgrammeEntity;
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
public interface ProgrammeRepository extends JpaRepository<ProgrammeEntity, UUID> {

    Optional<ProgrammeEntity> findByProgrammeCodeIgnoreCase(String programmeCode);

    boolean existsByProgrammeCodeIgnoreCase(String programmeCode);

    // Uniqueness check that skips the edited row
    boolean existsByProgrammeCodeIgnoreCaseAndProgrammeIdNot(String programmeCode, UUID programmeId);

    List<ProgrammeEntity> findAllByIsActiveTrueOrderByProgrammeNameAsc();

    @Query("SELECT p FROM ProgrammeEntity p WHERE " +
            "LOWER(p.programmeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.programmeCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ProgrammeEntity> searchProgrammes(@Param("search") String search, Pageable pageable);
}
