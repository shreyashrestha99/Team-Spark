package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.room.RoomRequestDto;
import com.Backend.Backend.dto.room.RoomResponseDto;
import com.Backend.Backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomService roomService;

    // Create a room
    @PostMapping
    public ResponseEntity<ApiResponseDto<RoomResponseDto>> create(
            @Valid @RequestBody RoomRequestDto request
    ) {
        RoomResponseDto response = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Room created successfully", response));
    }

    // Paged list, search and filters
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<RoomResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roomCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponseDto<RoomResponseDto> rooms =
                roomService.getAll(search, buildingId, roomType, minCapacity, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Rooms fetched successfully", rooms));
    }

    // Available rooms for dropdowns
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDto<List<RoomResponseDto>>> getAvailable() {
        List<RoomResponseDto> rooms = roomService.getAvailable();
        return ResponseEntity.ok(ApiResponseDto.success("Available rooms fetched successfully", rooms));
    }

    // Single room by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<RoomResponseDto>> getById(@PathVariable UUID id) {
        RoomResponseDto room = roomService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Room fetched successfully", room));
    }

    // Update a room
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<RoomResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoomRequestDto request
    ) {
        RoomResponseDto response = roomService.update(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Room updated successfully", response));
    }

    // Delete a room
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Room deleted successfully"));
    }
}
