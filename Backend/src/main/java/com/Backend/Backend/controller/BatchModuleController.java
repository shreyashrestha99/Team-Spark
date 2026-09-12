package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleRequestDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleResponseDto;
import com.Backend.Backend.service.BatchModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/batch-modules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BatchModuleController {

    private final BatchModuleService batchModuleService;

    // Assign a module to a batch
    @PostMapping
    public ResponseEntity<ApiResponseDto<BatchModuleResponseDto>> create(
            @Valid @RequestBody BatchModuleRequestDto request
    ) {
        BatchModuleResponseDto response = batchModuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Module assigned to batch successfully", response));
    }

    // Paged list with filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<BatchModuleResponseDto>>> getAll(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<BatchModuleResponseDto> assignments =
                batchModuleService.getAll(batchId, moduleId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Batch modules fetched successfully", assignments));
    }

    // Single assignment by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BatchModuleResponseDto>> getById(@PathVariable UUID id) {
        BatchModuleResponseDto assignment = batchModuleService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Batch module fetched successfully", assignment));
    }

    // Update an assignment
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BatchModuleResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BatchModuleRequestDto request
    ) {
        BatchModuleResponseDto response = batchModuleService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Batch module updated successfully", response));
    }

    // Unassign a module
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        batchModuleService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Module unassigned successfully"));
    }
}
