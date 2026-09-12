package com.Backend.Backend.config;

import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.BatchModuleEntity;
import com.Backend.Backend.entity.BuildingEntity;
import com.Backend.Backend.entity.HolidayEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.entity.ProgrammeEntity;
import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.StaffEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.BatchModuleRepository;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.BuildingRepository;
import com.Backend.Backend.repository.HolidayRepository;
import com.Backend.Backend.repository.ModuleRepository;
import com.Backend.Backend.repository.ProgrammeRepository;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StaffRepository;
import com.Backend.Backend.repository.StudentGroupRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimeSlotRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a working Islington-shaped college on first run: buildings, the weekly grid,
 * rooms with real seat grids, cohorts split into groups, and modules that already carry
 * a delivery pattern. The routine generator needs all of this to have anything to solve,
 * so a fresh database is demo-ready the moment the application starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    // Nepal works Sunday to Friday, with Saturday the weekend
    private static final DayOfWeek[] TEACHING_DAYS = {
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    };

    private static final String[] FIRST_NAMES = {
            "Aarav", "Pooja", "Sabin", "Kritee", "Nirajan", "Sneha", "Bibek", "Anjali",
            "Prashant", "Manisha", "Rohan", "Sarita", "Kiran", "Deepa", "Suman", "Rita",
            "Ashish", "Nisha", "Prabin", "Sunita", "Dipesh", "Alina", "Santosh", "Bina",
            "Milan", "Rekha", "Niraj", "Puja", "Ujjwal", "Samjhana"
    };

    private static final String[] LAST_NAMES = {
            "Sharma", "Shrestha", "Thapa", "Gurung", "Adhikari", "Karki", "Maharjan",
            "Tamang", "Rai", "Limbu", "Bhattarai", "Poudel", "Basnet", "Magar", "Joshi"
    };

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final TeacherRepository teacherRepository;
    private final StaffRepository staffRepository;
    private final ProgrammeRepository programmeRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;
    private final BatchModuleRepository batchModuleRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final HolidayRepository holidayRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultAdmin();
        seedTimeGrid();
        seedBuildingsAndRooms();
        seedHolidays();
        seedProgrammesAndBatches();
        seedStaffAndTeachers();
        seedModulesAndDelivery();
        seedCohorts();
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
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }

        RoleEntity adminRole = roleRepository.findByRoleName(RoleEnum.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new RoleEntity(RoleEnum.ROLE_ADMIN, "Administrator")));

        userRepository.save(UserEntity.builder()
                .fullName("System Administrator")
                .username("admin")
                .email("admin@islingtoncollege.edu.np")
                .password(passwordEncoder.encode("admin123"))
                .phoneNumber("+977-9800000000")
                .role(adminRole)
                .isActive(true)
                .build());

        log.info("Default Admin account created: username 'admin', password 'admin123'");
    }

    /**
     * The weekly grid the generator assigns sessions to.
     * Eight teaching periods a day with a lunch break the solver will not schedule into.
     */
    private void seedTimeGrid() {
        if (timeSlotRepository.count() > 0) {
            return;
        }

        LocalTime[][] periods = {
                {LocalTime.of(7, 0), LocalTime.of(8, 0)},
                {LocalTime.of(8, 0), LocalTime.of(9, 0)},
                {LocalTime.of(9, 0), LocalTime.of(10, 0)},
                {LocalTime.of(10, 0), LocalTime.of(11, 0)},
                {LocalTime.of(11, 0), LocalTime.of(12, 0)},
                {LocalTime.of(12, 0), LocalTime.of(13, 0)},
                {LocalTime.of(13, 0), LocalTime.of(14, 0)},
                {LocalTime.of(14, 0), LocalTime.of(15, 0)},
                {LocalTime.of(15, 0), LocalTime.of(16, 0)}
        };

        // Period 6 is lunch, kept in the grid so the UI can draw it but never scheduled
        int lunchPeriod = 6;

        for (DayOfWeek day : TEACHING_DAYS) {
            for (int index = 0; index < periods.length; index++) {
                int periodNumber = index + 1;
                timeSlotRepository.save(TimeSlotEntity.builder()
                        .dayOfWeek(day)
                        .periodNumber(periodNumber)
                        .startTime(periods[index][0])
                        .endTime(periods[index][1])
                        .isTeachingSlot(periodNumber != lunchPeriod)
                        .build());
            }
        }

        log.info("Seeded weekly grid: {} days x {} periods", TEACHING_DAYS.length, periods.length);
    }

    // Buildings and the rooms inside them, each with the seat grid exam seating needs
    private void seedBuildingsAndRooms() {
        if (buildingRepository.count() > 0) {
            return;
        }

        BuildingEntity himal = buildingRepository.save(BuildingEntity.builder()
                .buildingName("Himal Block")
                .buildingCode("HML")
                .floors(4)
                .isActive(true)
                .build());

        BuildingEntity ganesh = buildingRepository.save(BuildingEntity.builder()
                .buildingName("Ganesh Block")
                .buildingCode("GNS")
                .floors(3)
                .isActive(true)
                .build());

        saveRoom(himal, "HML-301", "Lecture Hall A", "LECTURE", 80, 10, 3, true, false, true);
        saveRoom(himal, "HML-302", "Lecture Hall B", "LECTURE", 60, 10, 3, true, false, true);
        saveRoom(himal, "HML-201", "Computer Lab 1", "LAB", 40, 8, 2, true, true, true);
        saveRoom(himal, "HML-202", "Computer Lab 2", "LAB", 40, 8, 2, true, true, true);
        saveRoom(himal, "HML-101", "Classroom 101", "CLASSROOM", 35, 7, 1, true, false, false);
        saveRoom(himal, "HML-102", "Classroom 102", "CLASSROOM", 35, 7, 1, false, false, false);

        saveRoom(ganesh, "GNS-201", "Lecture Hall C", "LECTURE", 100, 10, 2, true, false, true);
        saveRoom(ganesh, "GNS-101", "Computer Lab 3", "LAB", 30, 6, 1, true, true, true);
        saveRoom(ganesh, "GNS-102", "Classroom 201", "CLASSROOM", 30, 6, 2, false, false, false);
        saveRoom(ganesh, "GNS-103", "Networking Lab", "LAB", 25, 5, 1, true, true, true);

        log.info("Seeded 2 buildings and 10 rooms with seat grids");
    }

    // Rows are derived from capacity, exactly as the room service does it
    private void saveRoom(BuildingEntity building, String code, String name, String type,
                          int capacity, int seatsPerRow, int floor,
                          boolean projector, boolean computers, boolean airConditioned) {
        roomRepository.save(RoomEntity.builder()
                .building(building)
                .roomCode(code)
                .roomName(name)
                .roomType(type)
                .capacity(capacity)
                .seatsPerRow(seatsPerRow)
                .seatRows((int) Math.ceil((double) capacity / seatsPerRow))
                .examCapacityFactor(0.5)
                .floor(floor)
                .hasProjector(projector)
                .hasComputers(computers)
                .hasAc(airConditioned)
                .isAvailable(true)
                .build());
    }

    // Festival dates the generator skips when stamping the weekly pattern
    private void seedHolidays() {
        if (holidayRepository.count() > 0) {
            return;
        }

        int year = LocalDate.now().getYear();
        saveHoliday(LocalDate.of(year, 10, 2), "Ghatasthapana");
        saveHoliday(LocalDate.of(year, 10, 10), "Vijaya Dashami");
        saveHoliday(LocalDate.of(year, 10, 11), "Dashami Holiday");
        saveHoliday(LocalDate.of(year, 10, 31), "Laxmi Puja");
        saveHoliday(LocalDate.of(year, 11, 2), "Bhai Tika");

        log.info("Seeded festival holidays");
    }

    private void saveHoliday(LocalDate date, String name) {
        if (!holidayRepository.existsByHolidayDate(date)) {
            holidayRepository.save(HolidayEntity.builder()
                    .holidayDate(date)
                    .holidayName(name)
                    .build());
        }
    }

    // Programmes and the cohorts studying them
    private void seedProgrammesAndBatches() {
        if (batchRepository.count() > 0) {
            return;
        }

        ProgrammeEntity computing = saveProgramme("BSc (Hons) Computing", "BSC-COMP");
        ProgrammeEntity networking = saveProgramme("BSc (Hons) Networking & IT Security", "BSC-NET");

        saveBatch(computing, "Computing 2025 (L4)", 2025, 1, 1);
        saveBatch(computing, "Computing 2024 (L5)", 2024, 2, 3);
        saveBatch(networking, "Networking 2025 (L4)", 2025, 1, 1);

        log.info("Seeded programmes and batches");
    }

    private ProgrammeEntity saveProgramme(String name, String code) {
        return programmeRepository.findByProgrammeCodeIgnoreCase(code)
                .orElseGet(() -> programmeRepository.save(ProgrammeEntity.builder()
                        .programmeName(name)
                        .programmeCode(code)
                        .durationYears(3)
                        .isActive(true)
                        .build()));
    }

    private BatchEntity saveBatch(ProgrammeEntity programme, String name, int intakeYear,
                                  int yearOfStudy, int semester) {
        return batchRepository.save(BatchEntity.builder()
                .programme(programme)
                .batchName(name)
                .intakeYear(intakeYear)
                .yearOfStudy(yearOfStudy)
                .semester(semester)
                .isActive(true)
                .build());
    }

    // The RTE officer plus enough lecturers for the workload to spread
    private void seedStaffAndTeachers() {
        if (teacherRepository.count() > 0) {
            return;
        }

        RoleEntity teacherRole = roleRepository.findByRoleName(RoleEnum.ROLE_TEACHER).orElse(null);
        RoleEntity staffRole = roleRepository.findByRoleName(RoleEnum.ROLE_STAFF).orElse(null);

        if (teacherRole != null) {
            saveTeacher(teacherRole, "Dr. Ramesh Adhikari", "ramesh.adhikari", "Computing & IT", "Senior Lecturer");
            saveTeacher(teacherRole, "Er. Sunita Karki", "sunita.karki", "Software Engineering", "Module Leader");
            saveTeacher(teacherRole, "Mr. Bikash Thapa", "bikash.thapa", "Computing & IT", "Lecturer");
            saveTeacher(teacherRole, "Ms. Anita Gurung", "anita.gurung", "Networking", "Lecturer");
            saveTeacher(teacherRole, "Dr. Prakash Rai", "prakash.rai", "Computing & IT", "Associate Professor");
            saveTeacher(teacherRole, "Er. Nabin Joshi", "nabin.joshi", "Software Engineering", "Lecturer");
        }

        if (staffRole != null) {
            UserEntity officer = userRepository.save(UserEntity.builder()
                    .fullName("Bikash Maharjan")
                    .username("bikash.maharjan")
                    .email("bikash.rte@islingtoncollege.edu.np")
                    .password(passwordEncoder.encode("staff123"))
                    .phoneNumber("+977-9863344556")
                    .role(staffRole)
                    .isActive(true)
                    .build());

            staffRepository.save(StaffEntity.builder()
                    .user(officer)
                    .department("RTE Department")
                    .designation("Examination Officer")
                    .build());
        }

        log.info("Seeded lecturers and RTE staff");
    }

    private void saveTeacher(RoleEntity role, String fullName, String username,
                             String department, String designation) {
        UserEntity user = userRepository.save(UserEntity.builder()
                .fullName(fullName)
                .username(username)
                .email(username + "@islingtoncollege.edu.np")
                .password(passwordEncoder.encode("teacher123"))
                .phoneNumber("+977-985" + (1000000 + username.hashCode() % 1000000 + 1000000) % 10000000)
                .role(role)
                .isActive(true)
                .build());

        teacherRepository.save(TeacherEntity.builder()
                .user(user)
                .department(department)
                .designation(designation)
                .maxWeeklyHours(20)
                .build());
    }

    /**
     * Modules plus the weekly delivery pattern the generator expands.
     * A lecture for the whole cohort, then a tutorial and a workshop for each group.
     */
    private void seedModulesAndDelivery() {
        if (moduleRepository.count() > 0) {
            return;
        }

        List<BatchEntity> batches = batchRepository.findAll();
        List<TeacherEntity> teachers = teacherRepository.findAll();

        BatchEntity computingL4 = findBatch(batches, "Computing 2025 (L4)");
        BatchEntity computingL5 = findBatch(batches, "Computing 2024 (L5)");
        BatchEntity networkingL4 = findBatch(batches, "Networking 2025 (L4)");

        List<ModuleEntity> level4 = List.of(
                saveModule("CS4001", "Programming", 20, 1, 1),
                saveModule("CS4002", "Computer Systems", 20, 1, 1),
                saveModule("CS4003", "Web Design and Development", 20, 1, 1),
                saveModule("CS4004", "Mathematics for Computing", 20, 1, 1),
                saveModule("CS4005", "Introduction to Networking", 20, 1, 1)
        );

        List<ModuleEntity> level5 = List.of(
                saveModule("CS5001", "Software Engineering", 20, 2, 3),
                saveModule("CS5002", "Database Systems", 20, 2, 3),
                saveModule("CS5003", "Object Oriented Programming", 20, 2, 3),
                saveModule("CS5004", "Operating Systems", 20, 2, 3)
        );

        List<ModuleEntity> networkModules = List.of(
                saveModule("NW4001", "Network Fundamentals", 20, 1, 1),
                saveModule("NW4002", "IT Security Principles", 20, 1, 1),
                saveModule("NW4003", "Routing and Switching", 20, 1, 1)
        );

        assignModules(computingL4, level4, teachers, 0);
        assignModules(computingL5, level5, teachers, 2);
        assignModules(networkingL4, networkModules, teachers, 3);

        log.info("Seeded modules and weekly delivery patterns");
    }

    private ModuleEntity saveModule(String code, String name, int credits, int yearOfStudy, int semester) {
        return moduleRepository.save(ModuleEntity.builder()
                .moduleCode(code)
                .moduleName(name)
                .credits(credits)
                .yearOfStudy(yearOfStudy)
                .semester(semester)
                .isActive(true)
                .build());
    }

    // Spreads modules over the lecturers so no one starts out overloaded
    private void assignModules(BatchEntity batch, List<ModuleEntity> modules,
                               List<TeacherEntity> teachers, int teacherOffset) {
        if (batch == null || teachers.isEmpty()) {
            return;
        }

        for (int index = 0; index < modules.size(); index++) {
            TeacherEntity teacher = teachers.get((index + teacherOffset) % teachers.size());

            batchModuleRepository.save(BatchModuleEntity.builder()
                    .batch(batch)
                    .module(modules.get(index))
                    .teacher(teacher)
                    .lectureSessionsPerWeek(1)
                    .lectureDurationMinutes(120)
                    .tutorialSessionsPerWeek(1)
                    .tutorialDurationMinutes(60)
                    .workshopSessionsPerWeek(1)
                    .workshopDurationMinutes(120)
                    .splitTutorialByGroup(true)
                    .splitWorkshopByGroup(true)
                    .build());
        }
    }

    // Students, split into the tutorial and workshop groups the generator schedules against
    private void seedCohorts() {
        if (studentRepository.count() > 0) {
            return;
        }

        RoleEntity studentRole = roleRepository.findByRoleName(RoleEnum.ROLE_STUDENT).orElse(null);
        if (studentRole == null) {
            return;
        }

        List<BatchEntity> batches = batchRepository.findAll();
        int studentNumber = 1;

        studentNumber = enrol(findBatch(batches, "Computing 2025 (L4)"), studentRole, 60, 2, studentNumber);
        studentNumber = enrol(findBatch(batches, "Computing 2024 (L5)"), studentRole, 45, 2, studentNumber);
        enrol(findBatch(batches, "Networking 2025 (L4)"), studentRole, 30, 1, studentNumber);

        log.info("Seeded student cohorts and their tutorial groups");
    }

    /**
     * Creates a cohort, its groups, and spreads the students round-robin across them,
     * which keeps every group within one student of the others.
     */
    private int enrol(BatchEntity batch, RoleEntity studentRole, int headcount, int groupCount, int startNumber) {
        if (batch == null) {
            return startNumber;
        }

        List<StudentGroupEntity> groups = new ArrayList<>();
        for (int index = 0; index < groupCount; index++) {
            groups.add(studentGroupRepository.save(StudentGroupEntity.builder()
                    .batch(batch)
                    .groupName("Group " + (char) ('A' + index))
                    .build()));
        }

        int studentNumber = startNumber;
        for (int index = 0; index < headcount; index++) {
            // Keyed off the running number rather than the index, so two cohorts never
            // line up name-for-name and a shared exam hall reads as real people
            String firstName = FIRST_NAMES[studentNumber % FIRST_NAMES.length];
            String lastName = LAST_NAMES[(studentNumber * 7 / FIRST_NAMES.length + studentNumber)
                    % LAST_NAMES.length];
            String username = (firstName + "." + lastName + studentNumber).toLowerCase();

            UserEntity user = userRepository.save(UserEntity.builder()
                    .fullName(firstName + " " + lastName)
                    .username(username)
                    .email(username + "@islington.edu.np")
                    .password(passwordEncoder.encode("student123"))
                    .phoneNumber("+977-98" + String.format("%08d", 10000000 + studentNumber))
                    .role(studentRole)
                    .isActive(true)
                    .build());

            studentRepository.save(StudentEntity.builder()
                    .user(user)
                    .batch(batch)
                    .studentGroup(groups.get(index % groups.size()))
                    .studentNumber(String.format("NP03CS4S%06d", studentNumber))
                    .registrationNumber(String.format("REG-%d-%04d", batch.getIntakeYear(), index + 1))
                    .status("ACTIVE")
                    .build());

            studentNumber++;
        }

        return studentNumber;
    }

    private BatchEntity findBatch(List<BatchEntity> batches, String name) {
        return batches.stream()
                .filter(batch -> name.equals(batch.getBatchName()))
                .findFirst()
                .orElse(null);
    }
}
