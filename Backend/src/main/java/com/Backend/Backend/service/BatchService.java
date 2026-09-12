package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batch.BatchRequestDto;
import com.Backend.Backend.dto.batch.BatchResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ProgrammeEntity;
import com.Backend.Backend.repository.BatchRepository;
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
public class BatchService {

    private final BatchRepository batchRepository;
    private final ProgrammeRepository programmeRepository;

    // Create a new batch
    @Transactional
    public BatchResponseDto create(BatchRequestDto request) {
        String name = request.getBatchName().trim();

        if (batchRepository.existsByBatchNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Batch name '" + name + "' already exists");
        }

        validateDates(request);

        BatchEntity batch = BatchEntity.builder()
                .programme(findProgrammeOrThrow(request.getProgrammeId()))
                .batchName(name)
                .yearOfStudy(request.getYearOfStudy())
                .semester(request.getSemester())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return mapToDto(batchRepository.save(batch));
    }

    // Paged list with search and programme filter
    @Transactional(readOnly = true)
    public PageResponseDto<BatchResponseDto> getAll(
            String search,
            UUID programmeId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BatchEntity> result = batchRepository.searchBatches(blankIfNull(search), programmeId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Active batches for dropdowns
    @Transactional(readOnly = true)
    public List<BatchResponseDto> getActive() {
        return batchRepository.findAllByIsActiveTrueOrderByBatchNameAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one batch by id
    @Transactional(readOnly = true)
    public BatchResponseDto getById(UUID batchId) {
        return mapToDto(findOrThrow(batchId));
    }

    // Update an existing batch
    @Transactional
    public BatchResponseDto update(UUID batchId, BatchRequestDto request) {
        BatchEntity batch = findOrThrow(batchId);
        String name = request.getBatchName().trim();

        if (batchRepository.existsByBatchNameIgnoreCaseAndBatchIdNot(name, batchId)) {
            throw new IllegalArgumentException("Batch name '" + name + "' already exists");
        }

        validateDates(request);

        batch.setProgramme(findProgrammeOrThrow(request.getProgrammeId()));
        batch.setBatchName(name);
        batch.setYearOfStudy(request.getYearOfStudy());
        batch.setSemester(request.getSemester());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            batch.setIsActive(request.getIsActive());
        }

        return mapToDto(batchRepository.save(batch));
    }

    // Delete only when nothing depends on it
    @Transactional
    public void delete(UUID batchId) {
        BatchEntity batch = findOrThrow(batchId);

        int students = batch.getStudents().size();
        if (students > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: batch still has " + students + " student(s). Reassign them first."
            );
        }

        int sessions = batch.getTimetableSessions().size();
        if (sessions > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: batch is used by " + sessions + " timetable session(s)."
            );
        }

        int exams = batch.getExams().size();
        if (exams > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: batch is used by " + exams + " exam(s)."
            );
        }

        batchRepository.delete(batch);
    }

    // Empty string keeps the query parameter typed
    private String blankIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    // Start date must precede end date
    private void validateDates(BatchRequestDto request) {
        if (request.getStartDate() != null
                && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    // Shared lookup with a clear error
    private BatchEntity findOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    // Resolve the parent programme
    private ProgrammeEntity findProgrammeOrThrow(UUID programmeId) {
        return programmeRepository.findById(programmeId)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found: " + programmeId));
    }

    // Entity to response DTO
    private BatchResponseDto mapToDto(BatchEntity batch) {
        ProgrammeEntity programme = batch.getProgramme();

        StringBuilder label = new StringBuilder(batch.getBatchName());
        if (batch.getYearOfStudy() != null) {
            label.append(" — Year ").append(batch.getYearOfStudy());
        }
        if (batch.getSemester() != null) {
            label.append(" / Sem ").append(batch.getSemester());
        }

        return BatchResponseDto.builder()
                .batchId(batch.getBatchId())
                .batchName(batch.getBatchName())
                .yearOfStudy(batch.getYearOfStudy())
                .semester(batch.getSemester())
                .startDate(batch.getStartDate())
                .endDate(batch.getEndDate())
                .isActive(batch.getIsActive())
                .programmeId(programme != null ? programme.getProgrammeId() : null)
                .programmeName(programme != null ? programme.getProgrammeName() : null)
                .programmeCode(programme != null ? programme.getProgrammeCode() : null)
                .studentCount(batch.getStudents() != null ? batch.getStudents().size() : 0)
                .label(label.toString())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }
}
