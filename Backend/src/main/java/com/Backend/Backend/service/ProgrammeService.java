package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.programme.ProgrammeRequestDto;
import com.Backend.Backend.dto.programme.ProgrammeResponseDto;
import com.Backend.Backend.entity.ProgrammeEntity;
import com.Backend.Backend.repository.ProgrammeRepository;
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
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;

    // Create a new programme
    @Transactional
    public ProgrammeResponseDto create(ProgrammeRequestDto request) {
        String code = request.getProgrammeCode().trim();

        if (programmeRepository.existsByProgrammeCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Programme code '" + code + "' already exists");
        }

        ProgrammeEntity programme = ProgrammeEntity.builder()
                .programmeName(request.getProgrammeName().trim())
                .programmeCode(code)
                .durationYears(request.getDurationYears())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return mapToDto(programmeRepository.save(programme));
    }

    // Paged list with optional search
    @Transactional(readOnly = true)
    public PageResponseDto<ProgrammeResponseDto> getAll(
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
        Page<ProgrammeEntity> result = hasSearch
                ? programmeRepository.searchProgrammes(search.trim(), pageable)
                : programmeRepository.findAll(pageable);

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Active programmes for dropdowns
    @Transactional(readOnly = true)
    public List<ProgrammeResponseDto> getActive() {
        return programmeRepository.findAllByIsActiveTrueOrderByProgrammeNameAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one programme by id
    @Transactional(readOnly = true)
    public ProgrammeResponseDto getById(UUID programmeId) {
        return mapToDto(findOrThrow(programmeId));
    }

    // Update an existing programme
    @Transactional
    public ProgrammeResponseDto update(UUID programmeId, ProgrammeRequestDto request) {
        ProgrammeEntity programme = findOrThrow(programmeId);
        String code = request.getProgrammeCode().trim();

        if (programmeRepository.existsByProgrammeCodeIgnoreCaseAndProgrammeIdNot(code, programmeId)) {
            throw new IllegalArgumentException("Programme code '" + code + "' already exists");
        }

        programme.setProgrammeName(request.getProgrammeName().trim());
        programme.setProgrammeCode(code);
        programme.setDurationYears(request.getDurationYears());
        if (request.getIsActive() != null) {
            programme.setIsActive(request.getIsActive());
        }

        return mapToDto(programmeRepository.save(programme));
    }

    // Delete only when no batches depend on it
    @Transactional
    public void delete(UUID programmeId) {
        ProgrammeEntity programme = findOrThrow(programmeId);
        int batchCount = programme.getBatches().size();

        if (batchCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: programme still has " + batchCount + " batch(es). Remove them first."
            );
        }

        programmeRepository.delete(programme);
    }

    // Shared lookup with a clear error
    private ProgrammeEntity findOrThrow(UUID programmeId) {
        return programmeRepository.findById(programmeId)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found: " + programmeId));
    }

    // Entity to response DTO
    private ProgrammeResponseDto mapToDto(ProgrammeEntity programme) {
        return ProgrammeResponseDto.builder()
                .programmeId(programme.getProgrammeId())
                .programmeName(programme.getProgrammeName())
                .programmeCode(programme.getProgrammeCode())
                .durationYears(programme.getDurationYears())
                .isActive(programme.getIsActive())
                .batchCount(programme.getBatches() != null ? programme.getBatches().size() : 0)
                .createdAt(programme.getCreatedAt())
                .updatedAt(programme.getUpdatedAt())
                .build();
    }
}
