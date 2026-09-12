package com.Backend.Backend.service;

import com.Backend.Backend.dto.user.CreateUserRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.entity.RoleEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.repository.RoleRepository;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createStudent(CreateUserRequestDto request) {
        return createUser(request, RoleEnum.ROLE_STUDENT);
    }

    @Transactional
    public UserResponseDto createTeacher(CreateUserRequestDto request) {
        return createUser(request, RoleEnum.ROLE_TEACHER);
    }

    @Transactional
    public UserResponseDto createStaff(CreateUserRequestDto request) {
        return createUser(request, RoleEnum.ROLE_STAFF);
    }

    private UserResponseDto createUser(CreateUserRequestDto request, RoleEnum roleEnum) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        RoleEntity role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new IllegalStateException("Role " + roleEnum.name() + " not found in database"));

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .username(request.getUsername().toLowerCase().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);

        return mapToUserResponseDto(user);
    }

    public UserResponseDto mapToUserResponseDto(UserEntity user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().getRoleName().name() : null)
                .isActive(user.getIsActive())
                .build();
    }
}
