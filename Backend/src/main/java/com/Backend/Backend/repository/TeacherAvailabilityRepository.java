package com.Backend.Backend.repository;

import com.Backend.Backend.entity.TeacherAvailabilityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailabilityEntity, UUID> {

    List<TeacherAvailabilityEntity> findAllByTeacher_TeacherId(UUID teacherId);

    @Query("SELECT a FROM TeacherAvailabilityEntity a WHERE " +
            "(:teacherId IS NULL OR a.teacher.teacherId = :teacherId) AND " +
            "(:type = '' OR UPPER(a.availabilityType) = UPPER(:type))")
    Page<TeacherAvailabilityEntity> searchAvailability(
            @Param("teacherId") UUID teacherId,
            @Param("type") String type,
            Pageable pageable
    );
}
