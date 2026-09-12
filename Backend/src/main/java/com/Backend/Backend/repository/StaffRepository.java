package com.Backend.Backend.repository;

import com.Backend.Backend.entity.StaffEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, UUID> {

    Optional<StaffEntity> findByUser_UserId(UUID userId);

    @Query("SELECT s FROM StaffEntity s WHERE " +
            "LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.designation) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<StaffEntity> searchStaff(@Param("search") String search, Pageable pageable);
}
