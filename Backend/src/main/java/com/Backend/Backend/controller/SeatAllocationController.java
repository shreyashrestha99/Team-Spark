package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.seat.SeatAllocationRequestDto;
import com.Backend.Backend.dto.seat.SeatAllocationResponseDto;
import com.Backend.Backend.service.SeatAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/seat-allocations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SeatAllocationController {

    private final SeatAllocationService seatAllocationService;

    // Seat a student
    @PostMapping
    public ResponseEntity<ApiResponseDto<SeatAllocationResponseDto>> create(
            @Valid @RequestBody SeatAllocationRequestDto request
    ) {
        SeatAllocationResponseDto response = seatAllocationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Seat allocated successfully", response));
    }

    // Paged list with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<SeatAllocationResponseDto>>> getAll(
            @RequestParam(required = false) UUID examId,
            @RequestParam(required = false) UUID examRoomId,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "seatNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<SeatAllocationResponseDto> allocations = seatAllocationService.getAll(
                examId, examRoomId, studentId, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponseDto.success("Seat allocations fetched successfully", allocations));
    }

    // Full seating plan for one exam
    @GetMapping("/by-exam/{examId}")
    public ResponseEntity<ApiResponseDto<List<SeatAllocationResponseDto>>> getByExam(@PathVariable UUID examId) {
        List<SeatAllocationResponseDto> plan = seatAllocationService.getByExam(examId);
        return ResponseEntity.ok(ApiResponseDto.success("Seating plan fetched successfully", plan));
    }

    // Single allocation by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<SeatAllocationResponseDto>> getById(@PathVariable UUID id) {
        SeatAllocationResponseDto allocation = seatAllocationService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Seat allocation fetched successfully", allocation));
    }

    // Move a student's seat
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<SeatAllocationResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SeatAllocationRequestDto request
    ) {
        SeatAllocationResponseDto response = seatAllocationService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Seat allocation updated successfully", response));
    }

    // Free up a seat
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        seatAllocationService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Seat allocation removed successfully"));
    }
}
