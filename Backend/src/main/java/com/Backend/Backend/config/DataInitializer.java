package com.Backend.Backend.config;

import com.Backend.Backend.dto.exam.ExamRequestDto;
import com.Backend.Backend.dto.generation.GenerateRoutineRequestDto;
import com.Backend.Backend.dto.generation.GenerationResultDto;
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
import com.Backend.Backend.entity.TeacherAvailabilityEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.BatchModuleRepository;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.BuildingRepository;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.HolidayRepository;
import com.Backend.Backend.repository.ModuleRepository;
import com.Backend.Backend.repository.ProgrammeRepository;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StaffRepository;
import com.Backend.Backend.repository.StudentGroupRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherAvailabilityRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.TimeSlotRepository;
import com.Backend.Backend.repository.TimetableSessionRepository;
import com.Backend.Backend.repository.UserRepository;
import com.Backend.Backend.service.ExamService;
import com.Backend.Backend.service.RoutineGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds an Islington-shaped college on first run: three campus blocks, the weekly grid,
 * four London Met programmes with seven cohorts, the lecturers who teach them, and modules
 * that already carry a delivery pattern.
 *
 * It then generates every cohort's routine and schedules an exam week, so the admin,
 * student and lecturer dashboards all have real data the moment the application starts.
 *
 * Names, codes and room numbers are realistic placeholders, not Islington's own records.
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

    // Weeks of teaching the seeded routine covers before the exam week
    private static final int TEACHING_WEEKS = 12;

    private static final String EMAIL_DOMAIN = "@islingtoncollege.edu.np";

    private static final String[] FIRST_NAMES = {
            "Aarav", "Pooja", "Sabin", "Kritika", "Nirajan", "Sneha", "Bibek", "Anjali",
            "Prashant", "Manisha", "Rohan", "Sarita", "Kiran", "Deepa", "Suman", "Rita",
            "Ashish", "Nisha", "Prabin", "Sunita", "Dipesh", "Alina", "Santosh", "Bina",
            "Milan", "Rekha", "Niraj", "Puja", "Ujjwal", "Samjhana", "Aayush", "Srijana",
            "Rabin", "Asmita", "Sujan", "Prativa", "Anish", "Barsha", "Roshan", "Shreya"
    };

    private static final String[] LAST_NAMES = {
            "Sharma", "Shrestha", "Thapa", "Gurung", "Adhikari", "Karki", "Maharjan",
            "Tamang", "Rai", "Limbu", "Bhattarai", "Poudel", "Basnet", "Magar", "Joshi",
            "Khadka", "Dahal", "Pandey", "Bajracharya", "Shakya", "KC", "Pradhan"
    };

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final StaffRepository staffRepository;
    private final ProgrammeRepository programmeRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;
    private final BatchModuleRepository batchModuleRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final HolidayRepository holidayRepository;
    private final TimetableSessionRepository sessionRepository;
    private final ExamRepository examRepository;
    private final RoutineGenerationService routineGenerationService;
    private final ExamService examService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            seed();
        } catch (RuntimeException ex) {
            // Demo data is a convenience, so a seeding fault is logged rather than stopping startup
            log.error("Seeding stopped early: {}", ex.getMessage(), ex);
        }
    }

    private void seed() {
        seedRoles();
        seedDefaultAdmin();
        seedTimeGrid();
        seedBuildingsAndRooms();
        seedHolidays();
        seedProgrammesAndBatches();
        seedStaffAndTeachers();
        seedModulesAndDelivery();
        seedCohorts();
        seedRoutines();
        seedExamWeek();
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
                .email("admin" + EMAIL_DOMAIN)
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

    /**
     * Three campus blocks. Every cohort needs a hall for its whole-batch lectures and a lab
     * of thirty or more for each workshop group, and the exam hall gives exam week room to breathe.
     */
    private void seedBuildingsAndRooms() {
        if (buildingRepository.count() > 0) {
            return;
        }

        BuildingEntity himal = saveBuilding("Himal Block", "HML", 5);
        BuildingEntity annapurna = saveBuilding("Annapurna Block", "ANP", 4);
        BuildingEntity kailash = saveBuilding("Kailash Block", "KLS", 3);

        saveRoom(himal, "HML-G01", "Main Auditorium", "LECTURE", 150, 15, 0, true, false, true);
        saveRoom(himal, "HML-101", "Lecture Hall 1", "LECTURE", 100, 10, 1, true, false, true);
        saveRoom(himal, "HML-102", "Lecture Hall 2", "LECTURE", 80, 10, 1, true, false, true);
        saveRoom(himal, "HML-201", "Software Lab 1", "LAB", 40, 8, 2, true, true, true);
        saveRoom(himal, "HML-202", "Software Lab 2", "LAB", 40, 8, 2, true, true, true);
        saveRoom(himal, "HML-301", "Classroom 301", "CLASSROOM", 40, 8, 3, true, false, false);
        saveRoom(himal, "HML-302", "Classroom 302", "CLASSROOM", 40, 8, 3, true, false, false);

        saveRoom(annapurna, "ANP-101", "Lecture Hall 3", "LECTURE", 90, 10, 1, true, false, true);
        saveRoom(annapurna, "ANP-201", "Networking Lab", "LAB", 36, 6, 2, true, true, true);
        saveRoom(annapurna, "ANP-202", "Cyber Security Lab", "LAB", 36, 6, 2, true, true, true);
        saveRoom(annapurna, "ANP-301", "Classroom 301", "CLASSROOM", 35, 7, 3, false, false, false);
        saveRoom(annapurna, "ANP-302", "Classroom 302", "CLASSROOM", 35, 7, 3, false, false, false);

        saveRoom(kailash, "KLS-G01", "Examination Hall", "EXAM_HALL", 200, 20, 0, false, false, true);
        saveRoom(kailash, "KLS-101", "Multimedia Studio", "LAB", 32, 8, 1, true, true, true);
        saveRoom(kailash, "KLS-102", "Mac Lab", "LAB", 32, 8, 1, true, true, true);
        saveRoom(kailash, "KLS-201", "Business Seminar Room", "SEMINAR", 45, 9, 2, true, false, true);
        saveRoom(kailash, "KLS-202", "Seminar Room 2", "SEMINAR", 45, 9, 2, true, false, false);

        log.info("Seeded 3 buildings and 17 rooms with seat grids");
    }

    private BuildingEntity saveBuilding(String name, String code, int floors) {
        return buildingRepository.save(BuildingEntity.builder()
                .buildingName(name)
                .buildingCode(code)
                .floors(floors)
                .isActive(true)
                .build());
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

    // Public holidays the generator skips when stamping the weekly pattern
    private void seedHolidays() {
        if (holidayRepository.count() > 0) {
            return;
        }

        int year = LocalDate.now().getYear();
        saveHoliday(LocalDate.of(year, 9, 19), "Constitution Day");
        saveHoliday(LocalDate.of(year, 10, 2), "Ghatasthapana");
        saveHoliday(LocalDate.of(year, 10, 9), "Maha Navami");
        saveHoliday(LocalDate.of(year, 10, 10), "Vijaya Dashami");
        saveHoliday(LocalDate.of(year, 10, 11), "Dashami Holiday");
        saveHoliday(LocalDate.of(year, 10, 30), "Kukur Tihar");
        saveHoliday(LocalDate.of(year, 10, 31), "Laxmi Puja");
        saveHoliday(LocalDate.of(year, 11, 2), "Bhai Tika");
        saveHoliday(LocalDate.of(year, 11, 7), "Chhath Parva");
        saveHoliday(LocalDate.of(year, 12, 25), "Christmas Day");

        log.info("Seeded public holidays");
    }

    private void saveHoliday(LocalDate date, String name) {
        if (!holidayRepository.existsByHolidayDate(date)) {
            holidayRepository.save(HolidayEntity.builder()
                    .holidayDate(date)
                    .holidayName(name)
                    .build());
        }
    }

    // London Met awarded programmes and the cohorts studying them
    private void seedProgrammesAndBatches() {
        if (batchRepository.count() > 0) {
            return;
        }

        ProgrammeEntity computing = saveProgramme("BSc (Hons) Computing", "BSC-COMP", 3);
        ProgrammeEntity networking = saveProgramme("BSc (Hons) Computer Networking & IT Security", "BSC-NET", 3);
        ProgrammeEntity multimedia = saveProgramme("BSc (Hons) Multimedia Technologies", "BSC-MMT", 3);
        ProgrammeEntity business = saveProgramme("BA (Hons) Business Administration", "BA-BUS", 3);

        saveBatch(computing, "Computing 2025 (L4)", 2025, 1, 1);
        saveBatch(computing, "Computing 2024 (L5)", 2024, 2, 3);
        saveBatch(computing, "Computing 2023 (L6)", 2023, 3, 5);
        saveBatch(networking, "Networking 2025 (L4)", 2025, 1, 1);
        saveBatch(networking, "Networking 2024 (L5)", 2024, 2, 3);
        saveBatch(multimedia, "Multimedia 2025 (L4)", 2025, 1, 1);
        saveBatch(business, "Business 2025 (L4)", 2025, 1, 1);

        log.info("Seeded 4 programmes and 7 batches");
    }

    private ProgrammeEntity saveProgramme(String name, String code, int years) {
        return programmeRepository.findByProgrammeCodeIgnoreCase(code)
                .orElseGet(() -> programmeRepository.save(ProgrammeEntity.builder()
                        .programmeName(name)
                        .programmeCode(code)
                        .durationYears(years)
                        .isActive(true)
                        .build()));
    }

    private BatchEntity saveBatch(ProgrammeEntity programme, String name, int intakeYear,
                                  int yearOfStudy, int semester) {
        LocalDate start = LocalDate.of(intakeYear, 9, 1);

        return batchRepository.save(BatchEntity.builder()
                .programme(programme)
                .batchName(name)
                .intakeYear(intakeYear)
                .yearOfStudy(yearOfStudy)
                .semester(semester)
                .startDate(start)
                .endDate(start.plusYears(programme.getDurationYears()).minusDays(1))
                .isActive(true)
                .build());
    }

    // RTE officers plus a lecturer for every module, with a few declared availability windows
    private void seedStaffAndTeachers() {
        if (teacherRepository.count() > 0) {
            return;
        }

        RoleEntity teacherRole = roleRepository.findByRoleName(RoleEnum.ROLE_TEACHER).orElse(null);
        RoleEntity staffRole = roleRepository.findByRoleName(RoleEnum.ROLE_STAFF).orElse(null);

        if (teacherRole != null) {
            // One hash for the shared demo password, rather than one slow BCrypt call per account
            String password = passwordEncoder.encode("teacher123");

            String[][] lecturers = {
                    {"Dr. Ramesh Adhikari", "ramesh.adhikari", "School of Computing", "Head of School"},
                    {"Er. Sunita Karki", "sunita.karki", "School of Computing", "Senior Lecturer"},
                    {"Mr. Bikash Thapa", "bikash.thapa", "School of Computing", "Lecturer"},
                    {"Ms. Sarina Maharjan", "sarina.maharjan", "School of Computing", "Lecturer"},
                    {"Dr. Prakash Rai", "prakash.rai", "School of Computing", "Associate Professor"},
                    {"Er. Nabin Joshi", "nabin.joshi", "School of Computing", "Module Leader"},
                    {"Mr. Sujan Shakya", "sujan.shakya", "School of Computing", "Lecturer"},
                    {"Ms. Pratiksha Dahal", "pratiksha.dahal", "School of Computing", "Lecturer"},
                    {"Mr. Anil Bajracharya", "anil.bajracharya", "School of Computing", "Senior Lecturer"},
                    {"Ms. Anita Gurung", "anita.gurung", "School of Networking & Security", "Senior Lecturer"},
                    {"Er. Rajan Khadka", "rajan.khadka", "School of Networking & Security", "Lecturer"},
                    {"Mr. Dipendra Pandey", "dipendra.pandey", "School of Networking & Security", "Lecturer"},
                    {"Ms. Sabina Tamang", "sabina.tamang", "School of Networking & Security", "Lecturer"},
                    {"Mr. Rupesh Pradhan", "rupesh.pradhan", "School of Multimedia", "Senior Lecturer"},
                    {"Ms. Nirmala Shrestha", "nirmala.shrestha", "School of Multimedia", "Lecturer"},
                    {"Dr. Hari Bhattarai", "hari.bhattarai", "School of Business", "Head of School"},
                    {"Ms. Kabita Poudel", "kabita.poudel", "School of Business", "Senior Lecturer"},
                    {"Mr. Suresh KC", "suresh.kc", "School of Business", "Lecturer"},
                    {"Ms. Laxmi Basnet", "laxmi.basnet", "School of Business", "Lecturer"},
                    {"Mr. Pradeep Limbu", "pradeep.limbu", "School of Computing", "Visiting Lecturer"}
            };

            for (String[] lecturer : lecturers) {
                saveTeacher(teacherRole, lecturer[0], lecturer[1], lecturer[2], lecturer[3], password);
            }

            seedAvailability();
        }

        if (staffRole != null) {
            String password = passwordEncoder.encode("staff123");
            saveStaff(staffRole, "Bikash Maharjan", "bikash.maharjan", "Examination Officer", password);
            saveStaff(staffRole, "Rojina Shrestha", "rojina.shrestha", "Timetable Coordinator", password);
            saveStaff(staffRole, "Manoj Tamang", "manoj.tamang", "Resource Officer", password);
        }

        log.info("Seeded 20 lecturers and 3 RTE staff");
    }

    private void saveTeacher(RoleEntity role, String fullName, String username,
                             String department, String designation, String encodedPassword) {
        UserEntity user = userRepository.save(UserEntity.builder()
                .fullName(fullName)
                .username(username)
                .email(username + EMAIL_DOMAIN)
                .password(encodedPassword)
                .phoneNumber("+977-985" + String.format("%07d", Math.floorMod(username.hashCode(), 10_000_000)))
                .role(role)
                .isActive(true)
                .build());

        teacherRepository.save(TeacherEntity.builder()
                .user(user)
                .department(department)
                .designation(designation)
                // Visiting lecturers carry a lighter load
                .maxWeeklyHours(designation.startsWith("Visiting") ? 10 : 20)
                .build());
    }

    private void saveStaff(RoleEntity role, String fullName, String username,
                           String designation, String encodedPassword) {
        UserEntity user = userRepository.save(UserEntity.builder()
                .fullName(fullName)
                .username(username)
                .email(username + EMAIL_DOMAIN)
                .password(encodedPassword)
                .phoneNumber("+977-986" + String.format("%07d", Math.floorMod(username.hashCode(), 10_000_000)))
                .role(role)
                .isActive(true)
                .build());

        staffRepository.save(StaffEntity.builder()
                .user(user)
                .department("RTE Department")
                .designation(designation)
                .build());
    }

    // A handful of real-world constraints the generator has to respect
    private void seedAvailability() {
        saveAvailability("prakash.rai", DayOfWeek.FRIDAY, 7, 16, "UNAVAILABLE", "Research day");
        saveAvailability("pradeep.limbu", DayOfWeek.SUNDAY, 7, 16, "UNAVAILABLE", "Visiting lecturer, Sundays off");
        saveAvailability("pradeep.limbu", DayOfWeek.MONDAY, 7, 16, "UNAVAILABLE", "Visiting lecturer, Mondays off");
        saveAvailability("hari.bhattarai", DayOfWeek.WEDNESDAY, 13, 16, "UNAVAILABLE", "Academic board meetings");
        saveAvailability("sarina.maharjan", DayOfWeek.SUNDAY, 7, 12, "PREFERRED", "Prefers morning classes");
        saveAvailability("anita.gurung", DayOfWeek.TUESDAY, 7, 12, "PREFERRED", "Prefers morning classes");
    }

    private void saveAvailability(String username, DayOfWeek day, int fromHour, int toHour,
                                  String type, String note) {
        userRepository.findByUsername(username)
                .flatMap(user -> teacherRepository.findByUser_UserId(user.getUserId()))
                .ifPresent(teacher -> availabilityRepository.save(TeacherAvailabilityEntity.builder()
                        .teacher(teacher)
                        .dayOfWeek(day)
                        .startTime(LocalTime.of(fromHour, 0))
                        .endTime(LocalTime.of(toHour, 0))
                        .availabilityType(type)
                        .note(note)
                        .build()));
    }

    /**
     * Modules plus the weekly delivery pattern the generator expands. Computing, networking
     * and multimedia modules run a lecture, a tutorial and a lab workshop; business modules
     * have no lab work, and the final year project is lecture and supervision only.
     */
    private void seedModulesAndDelivery() {
        if (moduleRepository.count() > 0) {
            return;
        }

        Map<String, BatchEntity> batches = new HashMap<>();
        batchRepository.findAll().forEach(batch -> batches.put(batch.getBatchName(), batch));

        Map<String, ModuleEntity> modules = new HashMap<>();

        // Computing, Level 4
        assign(batches.get("Computing 2025 (L4)"), modules, Pattern.LAB,
                new String[]{"CS4001NI", "Programming", "sunita.karki"},
                new String[]{"CS4051NI", "Fundamentals of Computing", "bikash.thapa"},
                new String[]{"CC4002NI", "Information Systems", "sarina.maharjan"},
                new String[]{"CS4005NI", "Computer Hardware and Software Architectures", "sujan.shakya"},
                new String[]{"MA4001NI", "Logic and Problem Solving", "pratiksha.dahal"});

        // Computing, Level 5
        assign(batches.get("Computing 2024 (L5)"), modules, Pattern.LAB,
                new String[]{"CS5002NI", "Software Engineering", "nabin.joshi"},
                new String[]{"CC5051NI", "Databases", "anil.bajracharya"},
                new String[]{"CS5004NI", "Emerging Programming Platforms and Technologies", "prakash.rai"},
                new String[]{"CS5001NI", "Networks and Operating Systems", "rajan.khadka"});

        // Computing, Level 6
        assign(batches.get("Computing 2023 (L6)"), modules, Pattern.LAB,
                new String[]{"CS6004NI", "Application Development", "ramesh.adhikari"},
                new String[]{"CC6001NI", "Advanced Programming", "pradeep.limbu"},
                new String[]{"CS6002NI", "Advanced Database Systems Development", "anil.bajracharya"});
        assign(batches.get("Computing 2023 (L6)"), modules, Pattern.PROJECT,
                new String[]{"CS6001NI", "Final Year Project", "ramesh.adhikari"});

        // Networking, Level 4, sharing two modules with Computing
        assign(batches.get("Networking 2025 (L4)"), modules, Pattern.LAB,
                new String[]{"CT4001NI", "Network Fundamentals", "anita.gurung"},
                new String[]{"CT4002NI", "Linux System Administration", "dipendra.pandey"},
                new String[]{"CC4002NI", "Information Systems", "sabina.tamang"},
                new String[]{"MA4001NI", "Logic and Problem Solving", "pratiksha.dahal"});

        // Networking, Level 5
        assign(batches.get("Networking 2024 (L5)"), modules, Pattern.LAB,
                new String[]{"CT5052NI", "Network Operating Systems", "rajan.khadka"},
                new String[]{"CT5053NI", "Cloud Computing and the Internet of Things", "dipendra.pandey"},
                new String[]{"CS5063NI", "Ethical Hacking and Cyber Security", "anita.gurung"},
                new String[]{"CC5051NI", "Databases", "sabina.tamang"});

        // Multimedia, Level 4
        assign(batches.get("Multimedia 2025 (L4)"), modules, Pattern.LAB,
                new String[]{"MM4001NI", "Digital Media Design", "rupesh.pradhan"},
                new String[]{"MM4002NI", "Photography and Visual Communication", "nirmala.shrestha"},
                new String[]{"MM4003NI", "3D Modelling Fundamentals", "rupesh.pradhan"},
                new String[]{"MM4004NI", "Web Design for Multimedia", "nirmala.shrestha"});

        // Business, Level 4, no lab work
        assign(batches.get("Business 2025 (L4)"), modules, Pattern.CLASSROOM,
                new String[]{"BA4001NI", "Principles of Management", "hari.bhattarai"},
                new String[]{"BA4002NI", "Financial Accounting", "kabita.poudel"},
                new String[]{"BA4003NI", "Business Economics", "suresh.kc"},
                new String[]{"BA4004NI", "Marketing Fundamentals", "laxmi.basnet"});

        log.info("Seeded {} modules and their weekly delivery patterns", modules.size());
    }

    // How a module is taught each week
    private enum Pattern {
        // Lecture, tutorial and a two hour lab workshop
        LAB,
        // Lecture and tutorial, no lab
        CLASSROOM,
        // Short lecture plus group supervision
        PROJECT
    }

    /**
     * Links each module to a batch with its lecturer and weekly pattern. A module shared by
     * two programmes, such as Information Systems, is created once and assigned to both.
     */
    private void assign(BatchEntity batch, Map<String, ModuleEntity> modules, Pattern pattern, String[]... rows) {
        if (batch == null) {
            return;
        }

        for (String[] row : rows) {
            ModuleEntity module = modules.computeIfAbsent(row[0], code -> moduleRepository.save(ModuleEntity.builder()
                    .moduleCode(code)
                    .moduleName(row[1])
                    .credits(pattern == Pattern.PROJECT ? 30 : 15)
                    .yearOfStudy(batch.getYearOfStudy())
                    .semester(batch.getSemester())
                    .isActive(true)
                    .build()));

            TeacherEntity teacher = userRepository.findByUsername(row[2])
                    .flatMap(user -> teacherRepository.findByUser_UserId(user.getUserId()))
                    .orElse(null);

            batchModuleRepository.save(BatchModuleEntity.builder()
                    .batch(batch)
                    .module(module)
                    .teacher(teacher)
                    .lectureSessionsPerWeek(1)
                    .lectureDurationMinutes(pattern == Pattern.PROJECT ? 60 : 120)
                    .tutorialSessionsPerWeek(1)
                    .tutorialDurationMinutes(60)
                    .workshopSessionsPerWeek(pattern == Pattern.LAB ? 1 : 0)
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

        Map<String, BatchEntity> batches = new HashMap<>();
        batchRepository.findAll().forEach(batch -> batches.put(batch.getBatchName(), batch));

        // One hash for the shared demo password, rather than one slow BCrypt call per student
        String password = passwordEncoder.encode("student123");
        int studentNumber = 1;

        studentNumber = enrol(batches.get("Computing 2025 (L4)"), "CP", studentRole, password, 90, 3, studentNumber);
        studentNumber = enrol(batches.get("Computing 2024 (L5)"), "CP", studentRole, password, 60, 2, studentNumber);
        studentNumber = enrol(batches.get("Computing 2023 (L6)"), "CP", studentRole, password, 50, 2, studentNumber);
        studentNumber = enrol(batches.get("Networking 2025 (L4)"), "NT", studentRole, password, 40, 2, studentNumber);
        studentNumber = enrol(batches.get("Networking 2024 (L5)"), "NT", studentRole, password, 30, 1, studentNumber);
        studentNumber = enrol(batches.get("Multimedia 2025 (L4)"), "MM", studentRole, password, 32, 1, studentNumber);
        studentNumber = enrol(batches.get("Business 2025 (L4)"), "BS", studentRole, password, 60, 2, studentNumber);

        log.info("Seeded {} students across 7 cohorts and their tutorial groups", studentNumber - 1);
    }

    /**
     * Creates a cohort, its groups, and spreads the students round-robin across them,
     * which keeps every group within one student of the others.
     */
    private int enrol(BatchEntity batch, String programmeTag, RoleEntity studentRole, String encodedPassword,
                      int headcount, int groupCount, int startNumber) {
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

        int intake = batch.getIntakeYear() % 100;
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
                    .email(username + EMAIL_DOMAIN)
                    .password(encodedPassword)
                    .phoneNumber("+977-98" + String.format("%08d", 10000000 + studentNumber))
                    .role(studentRole)
                    .isActive(true)
                    .build());

            studentRepository.save(StudentEntity.builder()
                    .user(user)
                    .batch(batch)
                    .studentGroup(groups.get(index % groups.size()))
                    .studentNumber(String.format("NP01%s4A%02d%04d", programmeTag, intake, index + 1))
                    .registrationNumber(String.format("LMU-%d-%05d", batch.getIntakeYear(), studentNumber))
                    .status("ACTIVE")
                    .build());

            studentNumber++;
        }

        return studentNumber;
    }

    /**
     * Generates every cohort's routine from this week onwards. The largest cohorts go first,
     * because they have the fewest halls that fit them and need first pick.
     */
    private void seedRoutines() {
        if (sessionRepository.count() > 0) {
            return;
        }

        LocalDate from = startOfWeek(LocalDate.now());
        LocalDate to = from.plusWeeks(TEACHING_WEEKS).minusDays(1);

        for (BatchEntity batch : batchesLargestFirst()) {
            try {
                GenerationResultDto result = routineGenerationService.generate(GenerateRoutineRequestDto.builder()
                        .batchId(batch.getBatchId())
                        .fromDate(from)
                        .toDate(to)
                        .replaceExisting(true)
                        .optimise(true)
                        .build());

                log.info("Seeded routine for {}: {}/{} placed, {} sessions",
                        batch.getBatchName(),
                        result.getRun().getRequirementsPlaced(),
                        result.getRun().getRequirementsTotal(),
                        result.getRun().getSessionsCreated());
            } catch (RuntimeException ex) {
                // A seeding problem should never stop the application from starting
                log.warn("Could not seed routine for {}: {}", batch.getBatchName(), ex.getMessage());
            }
        }
    }

    /**
     * An exam week straight after teaching. Creating each exam also picks its halls, seats
     * every candidate and rosters invigilators. Exams are spread so a cohort sits one paper a
     * day, and cohorts alternate between morning and afternoon sittings.
     */
    private void seedExamWeek() {
        if (examRepository.count() > 0) {
            return;
        }

        LocalDate examStart = startOfWeek(LocalDate.now()).plusWeeks(TEACHING_WEEKS);
        List<LocalDate> examDays = new ArrayList<>();
        for (LocalDate day = examStart; examDays.size() < 12; day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY) {
                examDays.add(day);
            }
        }

        List<BatchEntity> batches = batchesLargestFirst();
        int created = 0;

        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            BatchEntity batch = batches.get(batchIndex);
            boolean morning = batchIndex % 2 == 0;

            // Modules come back loaded and in code order, since the seeder runs outside a transaction
            List<BatchModuleEntity> taught = batchModuleRepository.findAllWithModuleByBatchId(batch.getBatchId());

            for (int moduleIndex = 0; moduleIndex < taught.size(); moduleIndex++) {
                ModuleEntity module = taught.get(moduleIndex).getModule();
                LocalDate date = examDays.get((batchIndex + moduleIndex) % examDays.size());

                try {
                    examService.create(ExamRequestDto.builder()
                            .moduleId(module.getModuleId())
                            .batchId(batch.getBatchId())
                            .examDate(date)
                            .startTime(morning ? LocalTime.of(10, 0) : LocalTime.of(14, 0))
                            .endTime(morning ? LocalTime.of(13, 0) : LocalTime.of(17, 0))
                            .examType("FINAL")
                            .autoAllocateSeating(true)
                            .build());
                    created++;
                } catch (RuntimeException ex) {
                    log.warn("Could not seed exam {} for {}: {}",
                            module.getModuleCode(), batch.getBatchName(), ex.getMessage());
                }
            }
        }

        log.info("Seeded exam week starting {}: {} exams, seated and rostered", examStart, created);
    }

    private List<BatchEntity> batchesLargestFirst() {
        List<BatchEntity> batches = new ArrayList<>(batchRepository.findAll());
        batches.sort(Comparator
                .comparingInt((BatchEntity batch) -> studentRepository.countByBatch_BatchId(batch.getBatchId()))
                .reversed()
                .thenComparing(BatchEntity::getBatchName));
        return batches;
    }

    // Nepal teaches Sunday to Friday, so a week starts on Sunday
    private LocalDate startOfWeek(LocalDate date) {
        int daysSinceSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : date.getDayOfWeek().getValue();
        return date.minusDays(daysSinceSunday);
    }
}
