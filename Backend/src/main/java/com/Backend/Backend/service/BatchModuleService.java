package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleRequestDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.BatchModuleEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.repository.BatchModuleRepository;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchModuleService {

    private final BatchModuleRepository batchModuleRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;

    // Assign a module to a batch
    @Transactional
    public BatchModuleResponseDto create(BatchModuleRequestDto request) {
        if (batchModuleRepository.existsByBatch_BatchIdAndModule_ModuleId(
                request.getBatchId(), request.getModuleId())) {
            throw new IllegalArgumentException("This module is already assigned to the batch");
        }

        BatchEntity batch = findBatchOrThrow(request.getBatchId());
        ModuleEntity module = findModuleOrThrow(request.getModuleId());

        BatchModuleEntity batchModule = BatchModuleEntity.builder()
                .batch(batch)
                .module(module)
                .build();

        return mapToDto(batchModuleRepository.save(batchModule));
    }

    // Paged list with batch and module filters
    @Transactional(readOnly = true)
    public PageResponseDto<BatchModuleResponseDto> getAll(
            UUID batchId,
            UUID moduleId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BatchModuleEntity> result = batchModuleRepository.searchBatchModules(batchId, moduleId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Fetch one assignment by id
    @Transactional(readOnly = true)
    public BatchModuleResponseDto getById(UUID batchModuleId) {
        return mapToDto(findOrThrow(batchModuleId));
    }

    // Move an assignment to another pair
    @Transactional
    public BatchModuleResponseDto update(UUID batchModuleId, BatchModuleRequestDto request) {
        BatchModuleEntity batchModule = findOrThrow(batchModuleId);

        boolean pairChanged = !batchModule.getBatch().getBatchId().equals(request.getBatchId())
                || !batchModule.getModule().getModuleId().equals(request.getModuleId());

        if (pairChanged && batchModuleRepository.existsByBatch_BatchIdAndModule_ModuleId(
                request.getBatchId(), request.getModuleId())) {
            throw new IllegalArgumentException("This module is already assigned to the batch");
        }

        batchModule.setBatch(findBatchOrThrow(request.getBatchId()));
        batchModule.setModule(findModuleOrThrow(request.getModuleId()));

        return mapToDto(batchModuleRepository.save(batchModule));
    }

    // Unassign a module from a batch
    @Transactional
    public void delete(UUID batchModuleId) {
        batchModuleRepository.delete(findOrThrow(batchModuleId));
    }

    // Shared lookup with a clear error
    private BatchModuleEntity findOrThrow(UUID batchModuleId) {
        return batchModuleRepository.findById(batchModuleId)
                .orElseThrow(() -> new IllegalArgumentException("Batch module not found: " + batchModuleId));
    }

    private BatchEntity findBatchOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    private ModuleEntity findModuleOrThrow(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
    }

    // Entity to response DTO
    private BatchModuleResponseDto mapToDto(BatchModuleEntity batchModule) {
        BatchEntity batch = batchModule.getBatch();
        ModuleEntity module = batchModule.getModule();

        return BatchModuleResponseDto.builder()
                .batchModuleId(batchModule.getBatchModuleId())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .moduleId(module != null ? module.getModuleId() : null)
                .moduleCode(module != null ? module.getModuleCode() : null)
                .moduleName(module != null ? module.getModuleName() : null)
                .credits(module != null ? module.getCredits() : null)
                .createdAt(batchModule.getCreatedAt())
                .build();
    }
}
