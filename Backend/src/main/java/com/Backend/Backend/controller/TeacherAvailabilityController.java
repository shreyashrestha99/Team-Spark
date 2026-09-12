package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.availability.TeacherAvailabilityRequestDto;
import com.Backend.Backend.dto.availability.TeacherAvailabilityResponseDto;
import com.Backend.Backend.service.TeacherAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/teacher-availability")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService availabilityService;

    // Record a window
    @PostMapping
    public ResponseEntity<ApiResponseDto<TeacherAvailabilityResponseDto>> create(
            @Valid @RequestBody TeacherAvailabilityRequestDto request
    ) {
        TeacherAvailabilityResponseDto response = availabilityService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Availability saved successfully", response));
    }

    // Paged list with lecturer and type filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<TeacherAvailabilityResponseDto>>> getAll(
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<TeacherAvailabilityResponseDto> windows =
                availabilityService.getAll(teacherId, type, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Availability fetched successfully", windows));
    }

    // Every window for one lecturer
    @GetMapping("/by-teacher/{teacherId}")
    public ResponseEntity<ApiResponseDto<List<TeacherAvailabilityResponseDto>>> getByTeacher(
            @PathVariable UUID teacherId
    ) {
        List<TeacherAvailabilityResponseDto> windows = availabilityService.getByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponseDto.success("Availability fetched successfully", windows));
    }

    // Single window by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TeacherAvailabilityResponseDto>> getById(@PathVariable UUID id) {
        TeacherAvailabilityResponseDto window = availabilityService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Availability fetched successfully", window));
    }

    // Update a window
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TeacherAvailabilityResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherAvailabilityRequestDto request
    ) {
        TeacherAvailabilityResponseDto response = availabilityService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Availability updated successfully", response));
    }

    // Delete a window
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        availabilityService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Availability deleted successfully"));
    }
}
