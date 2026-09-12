package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.module.ModuleRequestDto;
import com.Backend.Backend.dto.module.ModuleResponseDto;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.repository.ModuleRepository;
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
public class ModuleService {

    private final ModuleRepository moduleRepository;

    // Create a new module
    @Transactional
    public ModuleResponseDto create(ModuleRequestDto request) {
        String code = request.getModuleCode().trim();

        if (moduleRepository.existsByModuleCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Module code '" + code + "' already exists");
        }

        ModuleEntity module = ModuleEntity.builder()
                .moduleCode(code)
                .moduleName(request.getModuleName().trim())
                .credits(request.getCredits())
                .yearOfStudy(request.getYearOfStudy())
                .semester(request.getSemester())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return mapToDto(moduleRepository.save(module));
    }

    // Paged list with search and filters
    @Transactional(readOnly = true)
    public PageResponseDto<ModuleResponseDto> getAll(
            String search,
            Integer yearOfStudy,
            Integer semester,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        String term = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<ModuleEntity> result = moduleRepository.searchModules(term, yearOfStudy, semester, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Active modules for dropdowns
    @Transactional(readOnly = true)
    public List<ModuleResponseDto> getActive() {
        return moduleRepository.findAllByIsActiveTrueOrderByModuleCodeAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one module by id
    @Transactional(readOnly = true)
    public ModuleResponseDto getById(UUID moduleId) {
        return mapToDto(findOrThrow(moduleId));
    }

    // Update an existing module
    @Transactional
    public ModuleResponseDto update(UUID moduleId, ModuleRequestDto request) {
        ModuleEntity module = findOrThrow(moduleId);
        String code = request.getModuleCode().trim();

        if (moduleRepository.existsByModuleCodeIgnoreCaseAndModuleIdNot(code, moduleId)) {
            throw new IllegalArgumentException("Module code '" + code + "' already exists");
        }

        module.setModuleCode(code);
        module.setModuleName(request.getModuleName().trim());
        module.setCredits(request.getCredits());
        module.setYearOfStudy(request.getYearOfStudy());
        module.setSemester(request.getSemester());
        if (request.getIsActive() != null) {
            module.setIsActive(request.getIsActive());
        }

        return mapToDto(moduleRepository.save(module));
    }

    // Delete only when nothing schedules it
    @Transactional
    public void delete(UUID moduleId) {
        ModuleEntity module = findOrThrow(moduleId);

        int sessions = module.getTimetableSessions().size();
        if (sessions > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: module is used by " + sessions + " timetable session(s)."
            );
        }

        int exams = module.getExams().size();
        if (exams > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: module is used by " + exams + " exam(s)."
            );
        }

        moduleRepository.delete(module);
    }

    // Shared lookup with a clear error
    private ModuleEntity findOrThrow(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
    }

    // Entity to response DTO
    private ModuleResponseDto mapToDto(ModuleEntity module) {
        return ModuleResponseDto.builder()
                .moduleId(module.getModuleId())
                .moduleCode(module.getModuleCode())
                .moduleName(module.getModuleName())
                .credits(module.getCredits())
                .yearOfStudy(module.getYearOfStudy())
                .semester(module.getSemester())
                .isActive(module.getIsActive())
                .batchCount(module.getBatchModules() != null ? module.getBatchModules().size() : 0)
                .label(module.getModuleCode() + " — " + module.getModuleName())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
