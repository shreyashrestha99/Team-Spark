package com.Backend.Backend.repository;

import com.Backend.Backend.entity.StudentEntity;
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
public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {

    Optional<StudentEntity> findByUser_UserId(UUID userId);

    Optional<StudentEntity> findByStudentNumber(String studentNumber);

    List<StudentEntity> findAllByBatch_BatchId(UUID batchId);

    List<StudentEntity> findAllByStudentGroup_GroupId(UUID groupId);

    int countByBatch_BatchId(UUID batchId);

    int countByStudentGroup_GroupId(UUID groupId);

    boolean existsByStudentNumberIgnoreCase(String studentNumber);

    // Uniqueness check that skips the edited row
    boolean existsByStudentNumberIgnoreCaseAndStudentIdNot(String studentNumber, UUID studentId);

    @Query("SELECT s FROM StudentEntity s WHERE " +
            "LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<StudentEntity> searchStudents(@Param("search") String search, Pageable pageable);
}
