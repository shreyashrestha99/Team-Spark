package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.generation.GenerateRoutineRequestDto;
import com.Backend.Backend.dto.generation.GenerationResultDto;
import com.Backend.Backend.dto.generation.GenerationRunResponseDto;
import com.Backend.Backend.service.RoutineGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/routine")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoutineController {

    private final RoutineGenerationService routineGenerationService;

    // Build a clash-free routine for one batch
    @PostMapping("/generate")
    public ResponseEntity<ApiResponseDto<GenerationResultDto>> generate(
            @Valid @RequestBody GenerateRoutineRequestDto request
    ) {
        GenerationResultDto result = routineGenerationService.generate(request);

        String message = result.getUnplaced().isEmpty()
                ? "Routine generated with no conflicts"
                : "Routine generated, " + result.getUnplaced().size() + " session(s) need attention";

        return ResponseEntity.ok(ApiResponseDto.success(message, result));
    }

    // History of previous runs
    @GetMapping("/runs")
    public ResponseEntity<ApiResponseDto<PageResponseDto<GenerationRunResponseDto>>> getRuns(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<GenerationRunResponseDto> runs =
                routineGenerationService.getRuns(batchId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Generation runs fetched successfully", runs));
    }

    // Single run by id
    @GetMapping("/runs/{id}")
    public ResponseEntity<ApiResponseDto<GenerationRunResponseDto>> getRunById(@PathVariable UUID id) {
        GenerationRunResponseDto run = routineGenerationService.getRunById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Generation run fetched successfully", run));
    }

    // Undo a whole generated routine
    @DeleteMapping("/runs/{id}")
    public ResponseEntity<ApiResponseDto<Void>> rollback(@PathVariable UUID id) {
        routineGenerationService.rollback(id);
        return ResponseEntity.ok(ApiResponseDto.success("Routine rolled back successfully"));
    }
}
