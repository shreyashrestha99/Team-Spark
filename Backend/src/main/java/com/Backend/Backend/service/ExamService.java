package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.exam.ExamRequestDto;
import com.Backend.Backend.dto.exam.ExamResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;

    // Schedule a new exam
    @Transactional
    public ExamResponseDto create(ExamRequestDto request) {
        validateTimes(request);
        rejectIfBatchClashes(request, null);

        BatchEntity batch = findBatchOrThrow(request.getBatchId());

        ExamEntity exam = ExamEntity.builder()
                .module(findModuleOrThrow(request.getModuleId()))
                .batch(batch)
                .examDate(request.getExamDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(minutesBetween(request))
                .examType(trimOrNull(request.getExamType()))
                .totalStudents(batch.getStudents() != null ? batch.getStudents().size() : 0)
                .status(hasText(request.getStatus()) ? request.getStatus().trim() : "SCHEDULED")
                .build();

        return mapToDto(examRepository.save(exam));
    }

    // Paged exam list with filters
    @Transactional(readOnly = true)
    public PageResponseDto<ExamResponseDto> getAll(
            UUID batchId,
            UUID moduleId,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExamEntity> result = examRepository.searchExams(
                batchId, moduleId, fromDate, toDate, blankIfNull(status), pageable
        );

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Fetch one exam by id
    @Transactional(readOnly = true)
    public ExamResponseDto getById(UUID examId) {
        return mapToDto(findOrThrow(examId));
    }

    // Update, re-checking batch clashes
    @Transactional
    public ExamResponseDto update(UUID examId, ExamRequestDto request) {
        ExamEntity exam = findOrThrow(examId);

        validateTimes(request);
        rejectIfBatchClashes(request, examId);

        BatchEntity batch = findBatchOrThrow(request.getBatchId());

        exam.setModule(findModuleOrThrow(request.getModuleId()));
        exam.setBatch(batch);
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDurationMinutes(minutesBetween(request));
        exam.setExamType(trimOrNull(request.getExamType()));
        exam.setTotalStudents(batch.getStudents() != null ? batch.getStudents().size() : 0);
        if (hasText(request.getStatus())) {
            exam.setStatus(request.getStatus().trim());
        }

        return mapToDto(examRepository.save(exam));
    }

    // Delete the exam and its allocations
    @Transactional
    public void delete(UUID examId) {
        ExamEntity exam = findOrThrow(examId);

        int seats = exam.getSeatAllocations().size();
        if (seats > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: exam has " + seats + " seat allocation(s). Clear seating first."
            );
        }

        examRepository.delete(exam);
    }

    // One batch cannot sit two exams at once
    private void rejectIfBatchClashes(ExamRequestDto request, UUID excludeId) {
        List<ExamEntity> clashes = examRepository.findBatchClashes(
                request.getBatchId(),
                request.getExamDate(),
                request.getStartTime(),
                request.getEndTime(),
                excludeId
        );

        if (!clashes.isEmpty()) {
            ExamEntity clash = clashes.get(0);
            throw new IllegalArgumentException(
                    "Scheduling conflict: batch already sits " + clash.getModule().getModuleCode()
                            + " on " + clash.getExamDate() + " at " + clash.getStartTime() + "–" + clash.getEndTime()
            );
        }
    }

    // End time must follow start time
    private void validateTimes(ExamRequestDto request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    // Duration derived from the slot
    private int minutesBetween(ExamRequestDto request) {
        return (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
    }

    private ExamEntity findOrThrow(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    }

    private BatchEntity findBatchOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    private ModuleEntity findModuleOrThrow(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Empty string keeps the query parameter typed
    private String blankIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Entity to response DTO
    private ExamResponseDto mapToDto(ExamEntity exam) {
        ModuleEntity module = exam.getModule();
        BatchEntity batch = exam.getBatch();

        List<ExamRoomEntity> rooms = exam.getExamRooms();
        int allocatedCapacity = rooms == null ? 0 : rooms.stream()
                .mapToInt(r -> r.getAllocatedCapacity() != null ? r.getAllocatedCapacity() : 0)
                .sum();

        return ExamResponseDto.builder()
                .examId(exam.getExamId())
                .moduleId(module != null ? module.getModuleId() : null)
                .moduleCode(module != null ? module.getModuleCode() : null)
                .moduleName(module != null ? module.getModuleName() : null)
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .examType(exam.getExamType())
                .totalStudents(exam.getTotalStudents())
                .status(exam.getStatus())
                .roomCount(rooms != null ? rooms.size() : 0)
                .allocatedCapacity(allocatedCapacity)
                .invigilatorCount(exam.getInvigilators() != null ? exam.getInvigilators().size() : 0)
                .seatsAllocated(exam.getSeatAllocations() != null ? exam.getSeatAllocations().size() : 0)
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }
}
