package com.Backend.Backend.config;

import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ProgrammeEntity;
import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.entity.StaffEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ProgrammeRepository;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.StaffRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherRepository;
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
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StaffRepository staffRepository;
    private final ProgrammeRepository programmeRepository;
    private final BatchRepository batchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultAdmin();
        seedProgrammesAndBatches();
        seedSampleUsers();
    }

    private void seedProgrammesAndBatches() {
        if (batchRepository.count() > 0) {
            return;
        }

        ProgrammeEntity computing = programmeRepository.findByProgrammeCodeIgnoreCase("BSC-COMP")
                .orElseGet(() -> programmeRepository.save(ProgrammeEntity.builder()
                        .programmeName("BSc (Hons) Computing")
                        .programmeCode("BSC-COMP")
                        .durationYears(3)
                        .isActive(true)
                        .build()));

        ProgrammeEntity networking = programmeRepository.findByProgrammeCodeIgnoreCase("BSC-NET")
                .orElseGet(() -> programmeRepository.save(ProgrammeEntity.builder()
                        .programmeName("BSc (Hons) Networking & IT Security")
                        .programmeCode("BSC-NET")
                        .durationYears(3)
                        .isActive(true)
                        .build()));

        batchRepository.save(BatchEntity.builder()
                .programme(computing)
                .batchName("Computing 2023 (L4)")
                .yearOfStudy(1)
                .semester(1)
                .isActive(true)
                .build());
        batchRepository.save(BatchEntity.builder()
                .programme(computing)
                .batchName("Computing 2022 (L5)")
                .yearOfStudy(2)
                .semester(4)
                .isActive(true)
                .build());
        batchRepository.save(BatchEntity.builder()
                .programme(networking)
                .batchName("Networking 2022 (L5)")
                .yearOfStudy(2)
                .semester(4)
                .isActive(true)
                .build());

        log.info("Seeded default programmes and batches");
    }

    // Ensure every role row exists
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

    // Create default admin on first run
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

    // Seed demo students, teachers, staff
    private void seedSampleUsers() {
        if (userRepository.count() <= 1) {
            RoleEntity studentRole = roleRepository.findByRoleName(RoleEnum.ROLE_STUDENT).orElse(null);
            RoleEntity teacherRole = roleRepository.findByRoleName(RoleEnum.ROLE_TEACHER).orElse(null);
            RoleEntity staffRole = roleRepository.findByRoleName(RoleEnum.ROLE_STAFF).orElse(null);

            BatchEntity sampleBatch = batchRepository.findAllByIsActiveTrueOrderByBatchNameAsc()
                    .stream().findFirst().orElse(null);

            if (studentRole != null) {
                UserEntity s1 = userRepository.save(UserEntity.builder()
                        .fullName("Aarav Sharma")
                        .username("aarav.sharma")
                        .email("aarav@islington.edu.np")
                        .password(passwordEncoder.encode("student123"))
                        .phoneNumber("+977-9811122334")
                        .role(studentRole)
                        .isActive(true)
                        .build());
                studentRepository.save(StudentEntity.builder()
                        .user(s1)
                        .batch(sampleBatch)
                        .studentNumber("NP03CS4S210001")
                        .registrationNumber("REG-2024-001")
                        .status("ACTIVE")
                        .build());

                UserEntity s2 = userRepository.save(UserEntity.builder()
                        .fullName("Pooja Shrestha")
                        .username("pooja.shrestha")
                        .email("pooja@islington.edu.np")
                        .password(passwordEncoder.encode("student123"))
                        .phoneNumber("+977-9844455667")
                        .role(studentRole)
                        .isActive(true)
                        .build());
                studentRepository.save(StudentEntity.builder()
                        .user(s2)
                        .batch(sampleBatch)
                        .studentNumber("NP03CS4S210002")
                        .registrationNumber("REG-2024-002")
                        .status("ACTIVE")
                        .build());
            }

            if (teacherRole != null) {
                UserEntity t1 = userRepository.save(UserEntity.builder()
                        .fullName("Dr. Ramesh Adhikari")
                        .username("ramesh.adhikari")
                        .email("ramesh@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("teacher123"))
                        .phoneNumber("+977-9851122334")
                        .role(teacherRole)
                        .isActive(true)
                        .build());
                teacherRepository.save(TeacherEntity.builder()
                        .user(t1)
                        .department("Computing & IT")
                        .designation("Senior Lecturer")
                        .build());

                UserEntity t2 = userRepository.save(UserEntity.builder()
                        .fullName("Er. Sunita Karki")
                        .username("sunita.karki")
                        .email("sunita@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("teacher123"))
                        .phoneNumber("+977-9852233445")
                        .role(teacherRole)
                        .isActive(true)
                        .build());
                teacherRepository.save(TeacherEntity.builder()
                        .user(t2)
                        .department("Software Engineering")
                        .designation("Module Leader")
                        .build());
            }

            if (staffRole != null) {
                UserEntity st1 = userRepository.save(UserEntity.builder()
                        .fullName("Bikash Maharjan")
                        .username("bikash.maharjan")
                        .email("bikash.rte@islingtoncollege.edu.np")
                        .password(passwordEncoder.encode("staff123"))
                        .phoneNumber("+977-9863344556")
                        .role(staffRole)
                        .isActive(true)
                        .build());
                staffRepository.save(StaffEntity.builder()
                        .user(st1)
                        .department("RTE Department")
                        .designation("Examination Officer")
                        .build());
            }
            log.info("Sample students, teachers, and staff seeded successfully with entity records");
        }
    }
}
