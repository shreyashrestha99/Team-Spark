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
        seedSampleUsers();
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

    private void seedSampleUsers() {
        if (userRepository.count() <= 1) {
            RoleEntity studentRole = roleRepository.findByRoleName(RoleEnum.ROLE_STUDENT).orElse(null);
            RoleEntity teacherRole = roleRepository.findByRoleName(RoleEnum.ROLE_TEACHER).orElse(null);
            RoleEntity staffRole = roleRepository.findByRoleName(RoleEnum.ROLE_STAFF).orElse(null);

            if (studentRole != null) {
                userRepository.save(UserEntity.builder()
                        .fullName("Aarav Sharma")
                        .username("aarav.sharma")
                        .email("aarav@islington.edu.np")
                        .password(passwordEncoder.encode("student123"))
                        .phoneNumber("+977-9811122334")
                        .role(studentRole)
                        .isActive(true)
                        .build());
                userRepository.save(UserEntity.builder()
                        .fullName("Pooja Shrestha")
                        .username("pooja.shrestha")
                        .email("pooja@islington.edu.np")
                        .password(passwordEncoder.encode("student123"))
                        .phoneNumber("+977-9844455667")
                        .role(studentRole)
                        .isActive(true)
                        .build());
            }

            if (teacherRole != null) {
                userRepository.save(UserEntity.builder()
                        .fullName("Dr. Ramesh Adhikari")
                        .username("ramesh.adhikari")
                        .email("ramesh@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("teacher123"))
                        .phoneNumber("+977-9851122334")
                        .role(teacherRole)
                        .isActive(true)
                        .build());
                userRepository.save(UserEntity.builder()
                        .fullName("Er. Sunita Karki")
                        .username("sunita.karki")
                        .email("sunita@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("teacher123"))
                        .phoneNumber("+977-9852233445")
                        .role(teacherRole)
                        .isActive(true)
                        .build());
            }

            if (staffRole != null) {
                userRepository.save(UserEntity.builder()
                        .fullName("Bikash Maharjan")
                        .username("bikash.maharjan")
                        .email("bikash.rte@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("staff123"))
                        .phoneNumber("+977-9863344556")
                        .role(staffRole)
                        .isActive(true)
                        .build());
            }
            log.info("Sample students, teachers, and staff seeded successfully");
        }
    }
}
