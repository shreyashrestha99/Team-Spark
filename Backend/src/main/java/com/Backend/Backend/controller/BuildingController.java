package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.building.BuildingRequestDto;
import com.Backend.Backend.dto.building.BuildingResponseDto;
import com.Backend.Backend.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/buildings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BuildingController {

    private final BuildingService buildingService;

    // Create a building
    @PostMapping
    public ResponseEntity<ApiResponseDto<BuildingResponseDto>> create(
            @Valid @RequestBody BuildingRequestDto request
    ) {
        BuildingResponseDto response = buildingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Building created successfully", response));
    }

    // Paged, searchable list
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<BuildingResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponseDto<BuildingResponseDto> buildings =
                buildingService.getAll(search, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Buildings fetched successfully", buildings));
    }

    // Active buildings for dropdowns
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<BuildingResponseDto>>> getActive() {
        List<BuildingResponseDto> buildings = buildingService.getActive();
        return ResponseEntity.ok(ApiResponseDto.success("Active buildings fetched successfully", buildings));
    }

    // Single building by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BuildingResponseDto>> getById(@PathVariable UUID id) {
        BuildingResponseDto building = buildingService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Building fetched successfully", building));
    }

    // Update a building
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BuildingResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingRequestDto request
    ) {
        BuildingResponseDto response = buildingService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Building updated successfully", response));
    }

    // Delete a building
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        buildingService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Building deleted successfully"));
    }
}
