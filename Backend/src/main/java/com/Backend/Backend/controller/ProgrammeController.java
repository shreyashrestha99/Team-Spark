package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.programme.ProgrammeRequestDto;
import com.Backend.Backend.dto.programme.ProgrammeResponseDto;
import com.Backend.Backend.service.ProgrammeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/programmes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProgrammeController {

    private final ProgrammeService programmeService;

    // Create a programme
    @PostMapping
    public ResponseEntity<ApiResponseDto<ProgrammeResponseDto>> create(
            @Valid @RequestBody ProgrammeRequestDto request
    ) {
        ProgrammeResponseDto response = programmeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Programme created successfully", response));
    }

    // Paged, searchable list
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProgrammeResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<ProgrammeResponseDto> programmes =
                programmeService.getAll(search, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Programmes fetched successfully", programmes));
    }

    // Active programmes for dropdowns
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<ProgrammeResponseDto>>> getActive() {
        List<ProgrammeResponseDto> programmes = programmeService.getActive();
        return ResponseEntity.ok(ApiResponseDto.success("Active programmes fetched successfully", programmes));
    }

    // Single programme by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProgrammeResponseDto>> getById(@PathVariable UUID id) {
        ProgrammeResponseDto programme = programmeService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Programme fetched successfully", programme));
    }

    // Update a programme
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProgrammeResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProgrammeRequestDto request
    ) {
        ProgrammeResponseDto response = programmeService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Programme updated successfully", response));
    }

    // Delete a programme
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        programmeService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Programme deleted successfully"));
    }
}
