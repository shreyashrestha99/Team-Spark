package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.building.BuildingRequestDto;
import com.Backend.Backend.dto.building.BuildingResponseDto;
import com.Backend.Backend.entity.BuildingEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.repository.BuildingRepository;
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
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;

    // Create a new building
    @Transactional
    public BuildingResponseDto create(BuildingRequestDto request) {
        String code = request.getBuildingCode().trim();
        String name = request.getBuildingName().trim();

        if (buildingRepository.existsByBuildingCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Building code '" + code + "' already exists");
        }
        if (buildingRepository.existsByBuildingNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Building '" + name + "' already exists");
        }

        BuildingEntity building = BuildingEntity.builder()
                .buildingName(name)
                .buildingCode(code)
                .floors(request.getFloors())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return mapToDto(buildingRepository.save(building));
    }

    // Paged list with optional search
    @Transactional(readOnly = true)
    public PageResponseDto<BuildingResponseDto> getAll(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasSearch = search != null && !search.trim().isEmpty();
        Page<BuildingEntity> result = hasSearch
                ? buildingRepository.searchBuildings(search.trim(), pageable)
                : buildingRepository.findAll(pageable);

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Active buildings for dropdowns
    @Transactional(readOnly = true)
    public List<BuildingResponseDto> getActive() {
        return buildingRepository.findAllByIsActiveTrueOrderByBuildingNameAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one building by id
    @Transactional(readOnly = true)
    public BuildingResponseDto getById(UUID buildingId) {
        return mapToDto(findOrThrow(buildingId));
    }

    // Update an existing building
    @Transactional
    public BuildingResponseDto update(UUID buildingId, BuildingRequestDto request) {
        BuildingEntity building = findOrThrow(buildingId);
        String code = request.getBuildingCode().trim();
        String name = request.getBuildingName().trim();

        if (buildingRepository.existsByBuildingCodeIgnoreCaseAndBuildingIdNot(code, buildingId)) {
            throw new IllegalArgumentException("Building code '" + code + "' already exists");
        }
        if (buildingRepository.existsByBuildingNameIgnoreCaseAndBuildingIdNot(name, buildingId)) {
            throw new IllegalArgumentException("Building '" + name + "' already exists");
        }

        building.setBuildingName(name);
        building.setBuildingCode(code);
        building.setFloors(request.getFloors());
        if (request.getIsActive() != null) {
            building.setIsActive(request.getIsActive());
        }

        return mapToDto(buildingRepository.save(building));
    }

    // Delete only when no rooms sit inside it
    @Transactional
    public void delete(UUID buildingId) {
        BuildingEntity building = findOrThrow(buildingId);

        if (roomRepository.existsByBuilding_BuildingId(buildingId)) {
            throw new IllegalArgumentException(
                    "Cannot delete: building still has rooms. Move or remove them first."
            );
        }

        buildingRepository.delete(building);
    }

    // Shared lookup with a clear error
    private BuildingEntity findOrThrow(UUID buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found: " + buildingId));
    }

    // Entity to response DTO
    private BuildingResponseDto mapToDto(BuildingEntity building) {
        List<RoomEntity> rooms = building.getRooms() != null ? building.getRooms() : List.of();

        return BuildingResponseDto.builder()
                .buildingId(building.getBuildingId())
                .buildingName(building.getBuildingName())
                .buildingCode(building.getBuildingCode())
                .floors(building.getFloors())
                .isActive(building.getIsActive())
                .roomCount(rooms.size())
                .totalCapacity(rooms.stream()
                        .mapToInt(room -> room.getCapacity() != null ? room.getCapacity() : 0)
                        .sum())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }
}
