package com.Backend.Backend.config;

import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultAdmin();
    }

    private void seedRoles() {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (!roleRepository.existsByRoleName(roleEnum)) {
                RoleEntity role = RoleEntity.builder()
                        .roleName(roleEnum)
                        .description("Default " + roleEnum.name().replace("ROLE_", "") + " role")
                        .build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleEnum.name());
            }
        }
    }

    private void seedDefaultAdmin() {
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            RoleEntity adminRole = roleRepository.findByRoleName(RoleEnum.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new RoleEntity(RoleEnum.ROLE_ADMIN, "Administrator")));

            UserEntity admin = UserEntity.builder()
                    .fullName("System Administrator")
                    .username("admin")
                    .email("admin@islingtoncollege.edu.np")
                    .password(passwordEncoder.encode("admin123"))
                    .phoneNumber("+977-9800000000")
                    .role(adminRole)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Default Admin account created: username 'admin', password 'admin123'");
        }
    }
}
