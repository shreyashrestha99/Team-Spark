package com.Backend.Backend.repository;

import com.Backend.Backend.entity.ModuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, UUID> {

    boolean existsByModuleCodeIgnoreCase(String moduleCode);

    // Uniqueness check that skips the edited row
    boolean existsByModuleCodeIgnoreCaseAndModuleIdNot(String moduleCode, UUID moduleId);

    List<ModuleEntity> findAllByIsActiveTrueOrderByModuleCodeAsc();

    // Combined list, search, year and semester filter.
    // Search uses "" rather than null so Postgres can type the parameter.
    @Query("SELECT m FROM ModuleEntity m WHERE " +
            "(:search = '' OR LOWER(m.moduleCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:yearOfStudy IS NULL OR m.yearOfStudy = :yearOfStudy) AND " +
            "(:semester IS NULL OR m.semester = :semester)")
    Page<ModuleEntity> searchModules(
            @Param("search") String search,
            @Param("yearOfStudy") Integer yearOfStudy,
            @Param("semester") Integer semester,
            Pageable pageable
    );
}
