package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.user.CreateUserRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<UserResponseDto>>> getUsers(
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<UserResponseDto> users = userService.getUsers(role, search, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Users fetched successfully", users));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getStats() {
        Map<String, Object> stats = userService.getDashboardStats();
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard stats fetched successfully", stats));
    }
}
