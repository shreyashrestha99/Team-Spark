package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.examroom.ExamRoomRequestDto;
import com.Backend.Backend.dto.examroom.ExamRoomResponseDto;
import com.Backend.Backend.service.ExamRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/exam-rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExamRoomController {

    private final ExamRoomService examRoomService;

    // Allocate a room to an exam
    @PostMapping
    public ResponseEntity<ApiResponseDto<ExamRoomResponseDto>> create(
            @Valid @RequestBody ExamRoomRequestDto request
    ) {
        ExamRoomResponseDto response = examRoomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Room allocated successfully", response));
    }

    // Paged list with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ExamRoomResponseDto>>> getAll(
            @RequestParam(required = false) UUID examId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<ExamRoomResponseDto> allocations =
                examRoomService.getAll(examId, roomId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Exam rooms fetched successfully", allocations));
    }

    // All rooms for one exam
    @GetMapping("/by-exam/{examId}")
    public ResponseEntity<ApiResponseDto<List<ExamRoomResponseDto>>> getByExam(@PathVariable UUID examId) {
        List<ExamRoomResponseDto> allocations = examRoomService.getByExam(examId);
        return ResponseEntity.ok(ApiResponseDto.success("Exam rooms fetched successfully", allocations));
    }

    // Single allocation by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ExamRoomResponseDto>> getById(@PathVariable UUID id) {
        ExamRoomResponseDto allocation = examRoomService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Exam room fetched successfully", allocation));
    }

    // Update an allocation
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ExamRoomResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExamRoomRequestDto request
    ) {
        ExamRoomResponseDto response = examRoomService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Exam room updated successfully", response));
    }

    // Release a room
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        examRoomService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Room released successfully"));
    }
}
