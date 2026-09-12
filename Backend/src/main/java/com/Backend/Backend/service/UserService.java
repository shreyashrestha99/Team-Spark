package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.user.CreateUserRequestDto;
import com.Backend.Backend.dto.user.UpdateUserRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.entity.StaffEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.RoomRepository;
import com.Backend.Backend.repository.StaffRepository;
import com.Backend.Backend.repository.StudentRepository;
import com.Backend.Backend.repository.TeacherRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StaffRepository staffRepository;
    private final BatchRepository batchRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    // Create user + student record
    @Transactional
    public UserResponseDto createStudent(CreateUserRequestDto request) {
        UserEntity user = createBaseUser(request, RoleEnum.ROLE_STUDENT);

        String studentNumber = (request.getStudentNumber() != null && !request.getStudentNumber().trim().isEmpty())
                ? request.getStudentNumber().trim()
                : "STU-" + System.currentTimeMillis();

        if (studentRepository.existsByStudentNumberIgnoreCase(studentNumber)) {
            throw new IllegalArgumentException("Student number '" + studentNumber + "' already exists");
        }

        BatchEntity batch = null;
        if (request.getBatchId() != null) {
            batch = batchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected batch does not exist"));
        }

        StudentEntity student = StudentEntity.builder()
                .user(user)
                .batch(batch)
                .studentNumber(studentNumber)
                .registrationNumber(request.getRegistrationNumber() != null ? request.getRegistrationNumber().trim() : null)
                .status(request.getStatus() != null && !request.getStatus().trim().isEmpty() ? request.getStatus().trim() : "ACTIVE")
                .build();

        studentRepository.save(student);
        user.setStudent(student);

        return mapToUserResponseDto(user);
    }

    // Create user + teacher record
    @Transactional
    public UserResponseDto createTeacher(CreateUserRequestDto request) {
        UserEntity user = createBaseUser(request, RoleEnum.ROLE_TEACHER);

        TeacherEntity teacher = TeacherEntity.builder()
                .user(user)
                .department(request.getDepartment() != null ? request.getDepartment().trim() : null)
                .designation(request.getDesignation() != null ? request.getDesignation().trim() : null)
                .build();

        teacherRepository.save(teacher);
        user.setTeacher(teacher);

        return mapToUserResponseDto(user);
    }

    // Create user + staff record
    @Transactional
    public UserResponseDto createStaff(CreateUserRequestDto request) {
        UserEntity user = createBaseUser(request, RoleEnum.ROLE_STAFF);

        StaffEntity staff = StaffEntity.builder()
                .user(user)
                .department(request.getDepartment() != null ? request.getDepartment().trim() : null)
                .designation(request.getDesignation() != null ? request.getDesignation().trim() : null)
                .build();

        staffRepository.save(staff);
        user.setStaff(staff);

        return mapToUserResponseDto(user);
    }

    // Shared account creation + uniqueness checks
    private UserEntity createBaseUser(CreateUserRequestDto request, RoleEnum roleEnum) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        RoleEntity role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new IllegalStateException("Role " + roleEnum.name() + " not found in database"));

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName().trim())
                .username(request.getUsername().toLowerCase().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .role(role)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    // Paged list with role + search filters
    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> getUsers(
            RoleEnum roleEnum,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasSearch = search != null && !search.trim().isEmpty();
        Page<UserEntity> userPage;

        if (roleEnum != null && hasSearch) {
            userPage = userRepository.searchByRole(roleEnum, search.trim(), pageable);
        } else if (roleEnum != null) {
            userPage = userRepository.findByRole_RoleName(roleEnum, pageable);
        } else if (hasSearch) {
            userPage = userRepository.searchAll(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Page<UserResponseDto> dtoPage = userPage.map(this::mapToUserResponseDto);

        return PageResponseDto.from(dtoPage);
    }

    // Fetch one user by id
    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID userId) {
        return mapToUserResponseDto(findOrThrow(userId));
    }

    // Update account + role-specific details
    @Transactional
    public UserResponseDto update(UUID userId, UpdateUserRequestDto request) {
        UserEntity user = findOrThrow(userId);
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(email, userId)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(trimOrNull(request.getPhoneNumber()));
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (user.getStudent() != null) {
            updateStudent(user.getStudent(), request);
        }
        if (user.getTeacher() != null) {
            applyDepartmentAndDesignation(request, user.getTeacher()::setDepartment, user.getTeacher()::setDesignation);
        }
        if (user.getStaff() != null) {
            applyDepartmentAndDesignation(request, user.getStaff()::setDepartment, user.getStaff()::setDesignation);
        }

        return mapToUserResponseDto(userRepository.save(user));
    }

    // Delete when nothing depends on the account
    @Transactional
    public void delete(UUID userId, String currentUsername) {
        UserEntity user = findOrThrow(userId);

        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        if (user.getRole() != null && user.getRole().getRoleName() == RoleEnum.ROLE_ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be deleted");
        }

        if (user.getTeacher() != null) {
            int sessions = user.getTeacher().getTimetableSessions().size();
            if (sessions > 0) {
                throw new IllegalArgumentException(
                        "Cannot delete: teacher still has " + sessions + " timetable session(s). Reassign them first."
                );
            }
        }

        if (user.getStudent() != null) {
            int seats = user.getStudent().getSeatAllocations().size();
            if (seats > 0) {
                throw new IllegalArgumentException(
                        "Cannot delete: student has " + seats + " exam seat allocation(s). Clear them first."
                );
            }
        }

        userRepository.delete(user);
    }

    // Student number, batch and status changes
    private void updateStudent(StudentEntity student, UpdateUserRequestDto request) {
        if (hasText(request.getStudentNumber())) {
            String number = request.getStudentNumber().trim();
            if (studentRepository.existsByStudentNumberIgnoreCaseAndStudentIdNot(number, student.getStudentId())) {
                throw new IllegalArgumentException("Student number '" + number + "' already exists");
            }
            student.setStudentNumber(number);
        }

        // Explicit blank clears; omitted leaves unchanged
        if (request.getRegistrationNumber() != null) {
            student.setRegistrationNumber(trimOrNull(request.getRegistrationNumber()));
        }
        if (hasText(request.getStatus())) {
            student.setStatus(request.getStatus().trim());
        }
        if (request.getBatchId() != null) {
            BatchEntity batch = batchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected batch does not exist"));
            student.setBatch(batch);
        }
    }

    // Shared setter logic for teacher and staff
    private void applyDepartmentAndDesignation(
            UpdateUserRequestDto request,
            Consumer<String> setDepartment,
            Consumer<String> setDesignation
    ) {
        if (request.getDepartment() != null) {
            setDepartment.accept(trimOrNull(request.getDepartment()));
        }
        if (request.getDesignation() != null) {
            setDesignation.accept(trimOrNull(request.getDesignation()));
        }
    }

    // Shared lookup with a clear error
    private UserEntity findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Top-level counts for dashboard cards
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        long studentCount = studentRepository.count();
        long teacherCount = teacherRepository.count();
        long staffCount = staffRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("students", studentCount);
        stats.put("teachers", teacherCount);
        stats.put("staff", staffCount);
        stats.put("rooms", roomRepository.count());
        stats.put("conflicts", 0);
        return stats;
    }

    // Flatten user + role details into DTO
    public UserResponseDto mapToUserResponseDto(UserEntity user) {
        UserResponseDto.UserResponseDtoBuilder builder = UserResponseDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().getRoleName().name() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt());

        if (user.getStudent() != null) {
            StudentEntity student = user.getStudent();
            builder.studentId(student.getStudentId())
                    .studentNumber(student.getStudentNumber())
                    .registrationNumber(student.getRegistrationNumber())
                    .studentStatus(student.getStatus());

            if (student.getBatch() != null) {
                BatchEntity batch = student.getBatch();
                builder.batchId(batch.getBatchId())
                        .batchName(batch.getBatchName());
                if (batch.getProgramme() != null) {
                    builder.programmeName(batch.getProgramme().getProgrammeName());
                }
            }
        }

        if (user.getTeacher() != null) {
            builder.teacherId(user.getTeacher().getTeacherId())
                    .teacherDepartment(user.getTeacher().getDepartment())
                    .teacherDesignation(user.getTeacher().getDesignation());
        }

        if (user.getStaff() != null) {
            builder.staffId(user.getStaff().getStaffId())
                    .staffDepartment(user.getStaff().getDepartment())
                    .staffDesignation(user.getStaff().getDesignation());
        }

        return builder.build();
    }
}
