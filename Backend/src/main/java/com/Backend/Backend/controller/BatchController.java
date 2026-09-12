package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batch.BatchRequestDto;
import com.Backend.Backend.dto.batch.BatchResponseDto;
import com.Backend.Backend.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BatchController {

    private final BatchService batchService;

    // Create a batch
    @PostMapping
    public ResponseEntity<ApiResponseDto<BatchResponseDto>> create(
            @Valid @RequestBody BatchRequestDto request
    ) {
        BatchResponseDto response = batchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Batch created successfully", response));
    }

    // Paged list, search and filter
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<BatchResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID programmeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<BatchResponseDto> batches =
                batchService.getAll(search, programmeId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Batches fetched successfully", batches));
    }

    // Active batches for dropdowns
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<BatchResponseDto>>> getActive() {
        List<BatchResponseDto> batches = batchService.getActive();
        return ResponseEntity.ok(ApiResponseDto.success("Active batches fetched successfully", batches));
    }

    // Single batch by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BatchResponseDto>> getById(@PathVariable UUID id) {
        BatchResponseDto batch = batchService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Batch fetched successfully", batch));
    }

    // Update a batch
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BatchResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BatchRequestDto request
    ) {
        BatchResponseDto response = batchService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Batch updated successfully", response));
    }

    // Delete a batch
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        batchService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Batch deleted successfully"));
    }
}
