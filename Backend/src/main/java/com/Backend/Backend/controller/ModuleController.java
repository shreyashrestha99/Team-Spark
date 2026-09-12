package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.module.ModuleRequestDto;
import com.Backend.Backend.dto.module.ModuleResponseDto;
import com.Backend.Backend.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/modules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModuleController {

    private final ModuleService moduleService;

    // Create a module
    @PostMapping
    public ResponseEntity<ApiResponseDto<ModuleResponseDto>> create(
            @Valid @RequestBody ModuleRequestDto request
    ) {
        ModuleResponseDto response = moduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Module created successfully", response));
    }

    // Paged list, search and filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<ModuleResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer yearOfStudy,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "moduleCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<ModuleResponseDto> modules =
                moduleService.getAll(search, yearOfStudy, semester, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Modules fetched successfully", modules));
    }

    // Active modules for dropdowns
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<ModuleResponseDto>>> getActive() {
        List<ModuleResponseDto> modules = moduleService.getActive();
        return ResponseEntity.ok(ApiResponseDto.success("Active modules fetched successfully", modules));
    }

    // Single module by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ModuleResponseDto>> getById(@PathVariable UUID id) {
        ModuleResponseDto module = moduleService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Module fetched successfully", module));
    }

    // Update a module
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ModuleResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleRequestDto request
    ) {
        ModuleResponseDto response = moduleService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Module updated successfully", response));
    }

    // Delete a module
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        moduleService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Module deleted successfully"));
    }
}
