package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.timeslot.TimeSlotRequestDto;
import com.Backend.Backend.dto.timeslot.TimeSlotResponseDto;
import com.Backend.Backend.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/time-slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // Add a cell to the weekly grid
    @PostMapping
    public ResponseEntity<ApiResponseDto<TimeSlotResponseDto>> create(
            @Valid @RequestBody TimeSlotRequestDto request
    ) {
        TimeSlotResponseDto response = timeSlotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Time slot created successfully", response));
    }

    // Paged list for the admin table
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<TimeSlotResponseDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "periodNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<TimeSlotResponseDto> slots =
                timeSlotService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Time slots fetched successfully", slots));
    }

    // The whole grid, ordered for rendering
    @GetMapping("/grid")
    public ResponseEntity<ApiResponseDto<List<TimeSlotResponseDto>>> getGrid() {
        List<TimeSlotResponseDto> slots = timeSlotService.getGrid();
        return ResponseEntity.ok(ApiResponseDto.success("Grid fetched successfully", slots));
    }

    // Only the cells a session may occupy
    @GetMapping("/teaching")
    public ResponseEntity<ApiResponseDto<List<TimeSlotResponseDto>>> getTeachingSlots() {
        List<TimeSlotResponseDto> slots = timeSlotService.getTeachingSlots();
        return ResponseEntity.ok(ApiResponseDto.success("Teaching slots fetched successfully", slots));
    }

    // Single slot by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TimeSlotResponseDto>> getById(@PathVariable UUID id) {
        TimeSlotResponseDto slot = timeSlotService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Time slot fetched successfully", slot));
    }

    // Update a slot
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TimeSlotResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TimeSlotRequestDto request
    ) {
        TimeSlotResponseDto response = timeSlotService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Time slot updated successfully", response));
    }

    // Delete a slot
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        timeSlotService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Time slot deleted successfully"));
    }
}
