package com.Backend.Backend.repository;

import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByRoleName(RoleEnum roleName);
    boolean existsByRoleName(RoleEnum roleName);
}
