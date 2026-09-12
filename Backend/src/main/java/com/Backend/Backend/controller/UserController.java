package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batch.BatchResponseDto;
import com.Backend.Backend.dto.user.CreateUserRequestDto;
import com.Backend.Backend.dto.user.UpdateUserRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.enums.RoleEnum;
import com.Backend.Backend.service.BatchService;
import com.Backend.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final BatchService batchService;

    // Create a student account
    @PostMapping("/student")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createStudent(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Student created successfully", response));
    }

    // Create a teacher account
    @PostMapping("/teacher")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createTeacher(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Teacher created successfully", response));
    }

    // Create a staff account
    @PostMapping("/staff")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createStaff(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = userService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Staff created successfully", response));
    }

    // Paged, searchable user list
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

    // Active batches for the student form
    @GetMapping("/batches")
    public ResponseEntity<ApiResponseDto<List<BatchResponseDto>>> getBatches() {
        List<BatchResponseDto> batches = batchService.getActive();
        return ResponseEntity.ok(ApiResponseDto.success("Batches fetched successfully", batches));
    }

    // Dashboard summary counts
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getStats() {
        Map<String, Object> stats = userService.getDashboardStats();
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard stats fetched successfully", stats));
    }

    // Single user by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getById(@PathVariable UUID id) {
        UserResponseDto user = userService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("User fetched successfully", user));
    }

    // Update account + role details
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        UserResponseDto response = userService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("User updated successfully", response));
    }

    // Delete a user account
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        userService.delete(id, principal != null ? principal.getUsername() : "");
        return ResponseEntity.ok(ApiResponseDto.success("User deleted successfully"));
    }
}
