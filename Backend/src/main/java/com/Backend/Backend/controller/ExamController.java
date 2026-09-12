package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.exam.ExamRequestDto;
import com.Backend.Backend.dto.exam.ExamResponseDto;
import com.Backend.Backend.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExamController {

    private final ExamService examService;

    // Schedule an exam
    @PostMapping
    public ResponseEntity<ApiResponseDto<ExamResponseDto>> create(
            @Valid @RequestBody ExamRequestDto request
    ) {
        ExamResponseDto response = examService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Exam scheduled successfully", response));
    }

    // Paged exam list with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ExamResponseDto>>> getAll(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "examDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<ExamResponseDto> exams = examService.getAll(
                batchId, moduleId, fromDate, toDate, status, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponseDto.success("Exams fetched successfully", exams));
    }

    // Single exam by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ExamResponseDto>> getById(@PathVariable UUID id) {
        ExamResponseDto exam = examService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Exam fetched successfully", exam));
    }

    // Update an exam
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ExamResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExamRequestDto request
    ) {
        ExamResponseDto response = examService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Exam updated successfully", response));
    }

    // Delete an exam
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        examService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Exam deleted successfully"));
    }
}
