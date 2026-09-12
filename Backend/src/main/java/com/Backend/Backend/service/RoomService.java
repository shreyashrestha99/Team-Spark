package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.room.RoomRequestDto;
import com.Backend.Backend.dto.room.RoomResponseDto;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    // Create a new room
    @Transactional
    public RoomResponseDto create(RoomRequestDto request) {
        String code = request.getRoomCode().trim();

        if (roomRepository.existsByRoomCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Room code '" + code + "' already exists");
        }

        RoomEntity room = RoomEntity.builder()
                .roomCode(code)
                .roomName(request.getRoomName().trim())
                .roomType(trimOrNull(request.getRoomType()))
                .capacity(request.getCapacity())
                .floor(request.getFloor())
                .building(trimOrNull(request.getBuilding()))
                .hasProjector(Boolean.TRUE.equals(request.getHasProjector()))
                .hasComputers(Boolean.TRUE.equals(request.getHasComputers()))
                .hasAc(Boolean.TRUE.equals(request.getHasAc()))
                .isAvailable(request.getIsAvailable() == null || request.getIsAvailable())
                .build();

        return mapToDto(roomRepository.save(room));
    }

    // Paged list with search and filters
    @Transactional(readOnly = true)
    public PageResponseDto<RoomResponseDto> getAll(
            String search,
            String roomType,
            Integer minCapacity,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RoomEntity> result = roomRepository.searchRooms(
                trimOrNull(search),
                trimOrNull(roomType),
                minCapacity,
                pageable
        );

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Available rooms for dropdowns
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getAvailable() {
        return roomRepository.findAllByIsAvailableTrueOrderByRoomCodeAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one room by id
    @Transactional(readOnly = true)
    public RoomResponseDto getById(UUID roomId) {
        return mapToDto(findOrThrow(roomId));
    }

    // Update an existing room
    @Transactional
    public RoomResponseDto update(UUID roomId, RoomRequestDto request) {
        RoomEntity room = findOrThrow(roomId);
        String code = request.getRoomCode().trim();

        if (roomRepository.existsByRoomCodeIgnoreCaseAndRoomIdNot(code, roomId)) {
            throw new IllegalArgumentException("Room code '" + code + "' already exists");
        }

        room.setRoomCode(code);
        room.setRoomName(request.getRoomName().trim());
        room.setRoomType(trimOrNull(request.getRoomType()));
        room.setCapacity(request.getCapacity());
        room.setFloor(request.getFloor());
        room.setBuilding(trimOrNull(request.getBuilding()));

        // Flags only change when supplied
        if (request.getHasProjector() != null) room.setHasProjector(request.getHasProjector());
        if (request.getHasComputers() != null) room.setHasComputers(request.getHasComputers());
        if (request.getHasAc() != null) room.setHasAc(request.getHasAc());
        if (request.getIsAvailable() != null) room.setIsAvailable(request.getIsAvailable());

        return mapToDto(roomRepository.save(room));
    }

    // Delete only when nothing books it
    @Transactional
    public void delete(UUID roomId) {
        RoomEntity room = findOrThrow(roomId);

        int sessions = room.getTimetableSessions().size();
        if (sessions > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: room is used by " + sessions + " timetable session(s)."
            );
        }

        int examRooms = room.getExamRooms().size();
        if (examRooms > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: room is assigned to " + examRooms + " exam(s)."
            );
        }

        roomRepository.delete(room);
    }

    // Shared lookup with a clear error
    private RoomEntity findOrThrow(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Entity to response DTO
    private RoomResponseDto mapToDto(RoomEntity room) {
        StringBuilder label = new StringBuilder(room.getRoomCode());
        label.append(" — ").append(room.getRoomName());
        if (room.getCapacity() != null) {
            label.append(" (").append(room.getCapacity()).append(" seats)");
        }

        return RoomResponseDto.builder()
                .roomId(room.getRoomId())
                .roomCode(room.getRoomCode())
                .roomName(room.getRoomName())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .floor(room.getFloor())
                .building(room.getBuilding())
                .hasProjector(room.getHasProjector())
                .hasComputers(room.getHasComputers())
                .hasAc(room.getHasAc())
                .isAvailable(room.getIsAvailable())
                .label(label.toString())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
