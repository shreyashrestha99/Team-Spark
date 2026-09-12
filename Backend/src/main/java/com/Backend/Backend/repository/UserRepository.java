package com.Backend.Backend.repository;

import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
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
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<UserEntity> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    // Uniqueness check that skips the edited row
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, UUID userId);

    Page<UserEntity> findByRole_RoleName(RoleEnum roleName, Pageable pageable);

    @Query("SELECT u FROM UserEntity u WHERE u.role.roleName = :roleName AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserEntity> searchByRole(
            @Param("roleName") RoleEnum roleName,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<UserEntity> searchAll(
            @Param("search") String search,
            Pageable pageable
    );

    long countByRole_RoleName(RoleEnum roleName);

    // Everyone eligible for invigilation duty
    List<UserEntity> findAllByRole_RoleNameAndIsActiveTrue(RoleEnum roleName);
}
