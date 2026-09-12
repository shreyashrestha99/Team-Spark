package com.Backend.Backend.repository;

import com.Backend.Backend.entity.ProgrammeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgrammeRepository extends JpaRepository<ProgrammeEntity, UUID> {

    Optional<ProgrammeEntity> findByProgrammeCodeIgnoreCase(String programmeCode);

    boolean existsByProgrammeCodeIgnoreCase(String programmeCode);
}
