package com.Backend.Backend.repository;

import com.Backend.Backend.entity.TeacherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, UUID> {

    Optional<TeacherEntity> findByUser_UserId(UUID userId);

    @Query("SELECT t FROM TeacherEntity t WHERE " +
            "LOWER(t.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.user.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.department) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<TeacherEntity> searchTeachers(@Param("search") String search, Pageable pageable);
}
