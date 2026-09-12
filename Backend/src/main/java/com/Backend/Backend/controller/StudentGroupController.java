package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.studentgroup.StudentGroupRequestDto;
import com.Backend.Backend.dto.studentgroup.StudentGroupResponseDto;
import com.Backend.Backend.service.StudentGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/student-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    // Create a group
    @PostMapping
    public ResponseEntity<ApiResponseDto<StudentGroupResponseDto>> create(
            @Valid @RequestBody StudentGroupRequestDto request
    ) {
        StudentGroupResponseDto response = studentGroupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Group created successfully", response));
    }

    // Paged list with an optional batch filter
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<StudentGroupResponseDto>>> getAll(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "groupName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<StudentGroupResponseDto> groups =
                studentGroupService.getAll(batchId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Groups fetched successfully", groups));
    }

    // Groups of one batch for dropdowns
    @GetMapping("/by-batch/{batchId}")
    public ResponseEntity<ApiResponseDto<List<StudentGroupResponseDto>>> getByBatch(@PathVariable UUID batchId) {
        List<StudentGroupResponseDto> groups = studentGroupService.getByBatch(batchId);
        return ResponseEntity.ok(ApiResponseDto.success("Groups fetched successfully", groups));
    }

    // Split a batch into groups and spread its students across them
    @PostMapping("/auto-split")
    public ResponseEntity<ApiResponseDto<List<StudentGroupResponseDto>>> autoSplit(
            @RequestParam UUID batchId,
            @RequestParam(defaultValue = "2") int groupCount
    ) {
        List<StudentGroupResponseDto> groups = studentGroupService.autoSplit(batchId, groupCount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Batch split into " + groups.size() + " group(s)", groups));
    }

    // Single group by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<StudentGroupResponseDto>> getById(@PathVariable UUID id) {
        StudentGroupResponseDto group = studentGroupService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Group fetched successfully", group));
    }

    // Update a group
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<StudentGroupResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudentGroupRequestDto request
    ) {
        StudentGroupResponseDto response = studentGroupService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Group updated successfully", response));
    }

    // Delete a group
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        studentGroupService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Group deleted successfully"));
    }
}
