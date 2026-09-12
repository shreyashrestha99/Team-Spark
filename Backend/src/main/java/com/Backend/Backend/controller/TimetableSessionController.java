package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.timetable.ScheduleConflictDto;
import com.Backend.Backend.dto.timetable.TimetableSessionRequestDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.service.TimetableSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/timetable-sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TimetableSessionController {

    private final TimetableSessionService sessionService;

    // Create a clash-free session
    @PostMapping
    public ResponseEntity<ApiResponseDto<TimetableSessionResponseDto>> create(
            @Valid @RequestBody TimetableSessionRequestDto request
    ) {
        TimetableSessionResponseDto response = sessionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Session scheduled successfully", response));
    }

    // Paged timetable with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<TimetableSessionResponseDto>>> getAll(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sessionDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<TimetableSessionResponseDto> sessions = sessionService.getAll(
                batchId, moduleId, teacherId, roomId, fromDate, toDate, status,
                page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponseDto.success("Sessions fetched successfully", sessions));
    }

    // Dry-run clash check before saving
    @PostMapping("/check-conflicts")
    public ResponseEntity<ApiResponseDto<List<ScheduleConflictDto>>> checkConflicts(
            @Valid @RequestBody TimetableSessionRequestDto request,
            @RequestParam(required = false) UUID excludeSessionId
    ) {
        List<ScheduleConflictDto> conflicts = sessionService.checkConflicts(request, excludeSessionId);
        String message = conflicts.isEmpty() ? "No conflicts found" : "Conflicts detected";
        return ResponseEntity.ok(ApiResponseDto.success(message, conflicts));
    }

    // Single session by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TimetableSessionResponseDto>> getById(@PathVariable UUID id) {
        TimetableSessionResponseDto session = sessionService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Session fetched successfully", session));
    }

    // Update a session
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TimetableSessionResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TimetableSessionRequestDto request
    ) {
        TimetableSessionResponseDto response = sessionService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Session updated successfully", response));
    }

    // Delete a session
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        sessionService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Session deleted successfully"));
    }
}
