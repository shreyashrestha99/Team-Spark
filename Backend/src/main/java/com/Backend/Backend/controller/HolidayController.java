package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.holiday.HolidayRequestDto;
import com.Backend.Backend.dto.holiday.HolidayResponseDto;
import com.Backend.Backend.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HolidayController {

    private final HolidayService holidayService;

    // Record a date the generator skips
    @PostMapping
    public ResponseEntity<ApiResponseDto<HolidayResponseDto>> create(
            @Valid @RequestBody HolidayRequestDto request
    ) {
        HolidayResponseDto response = holidayService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Holiday created successfully", response));
    }

    // Paged list for the admin table
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<HolidayResponseDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "holidayDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<HolidayResponseDto> holidays =
                holidayService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Holidays fetched successfully", holidays));
    }

    // Every holiday, earliest first
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponseDto<List<HolidayResponseDto>>> getUpcoming() {
        List<HolidayResponseDto> holidays = holidayService.getUpcoming();
        return ResponseEntity.ok(ApiResponseDto.success("Holidays fetched successfully", holidays));
    }

    // Single holiday by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<HolidayResponseDto>> getById(@PathVariable UUID id) {
        HolidayResponseDto holiday = holidayService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Holiday fetched successfully", holiday));
    }

    // Update a holiday
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<HolidayResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HolidayRequestDto request
    ) {
        HolidayResponseDto response = holidayService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Holiday updated successfully", response));
    }

    // Delete a holiday
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        holidayService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Holiday deleted successfully"));
    }
}
