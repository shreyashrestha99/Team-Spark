package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.user.CreateUserRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping("/student")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createStudent(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Student created successfully", response));
    }

    @PostMapping("/teacher")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createTeacher(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Teacher created successfully", response));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createStaff(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Staff created successfully", response));
    }
}
