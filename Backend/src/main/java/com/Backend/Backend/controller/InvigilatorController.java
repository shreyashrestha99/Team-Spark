package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.invigilator.InvigilatorRequestDto;
import com.Backend.Backend.dto.invigilator.InvigilatorResponseDto;
import com.Backend.Backend.service.InvigilatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/invigilators")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InvigilatorController {

    private final InvigilatorService invigilatorService;

    // Assign an invigilator
    @PostMapping
    public ResponseEntity<ApiResponseDto<InvigilatorResponseDto>> create(
            @Valid @RequestBody InvigilatorRequestDto request
    ) {
        InvigilatorResponseDto response = invigilatorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Invigilator assigned successfully", response));
    }

    // Paged list with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<InvigilatorResponseDto>>> getAll(
            @RequestParam(required = false) UUID examId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<InvigilatorResponseDto> assignments =
                invigilatorService.getAll(examId, userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Invigilators fetched successfully", assignments));
    }

    // Duty roster for one exam
    @GetMapping("/by-exam/{examId}")
    public ResponseEntity<ApiResponseDto<List<InvigilatorResponseDto>>> getByExam(@PathVariable UUID examId) {
        List<InvigilatorResponseDto> roster = invigilatorService.getByExam(examId);
        return ResponseEntity.ok(ApiResponseDto.success("Duty roster fetched successfully", roster));
    }

    // Single assignment by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<InvigilatorResponseDto>> getById(@PathVariable UUID id) {
        InvigilatorResponseDto assignment = invigilatorService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Invigilator fetched successfully", assignment));
    }

    // Update an assignment
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<InvigilatorResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody InvigilatorRequestDto request
    ) {
        InvigilatorResponseDto response = invigilatorService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Invigilator updated successfully", response));
    }

    // Remove from duty
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        invigilatorService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Invigilator removed successfully"));
    }
}
