package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleRequestDto;
import com.Backend.Backend.dto.batchmodule.BatchModuleResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.BatchModuleEntity;
import com.Backend.Backend.entity.ModuleEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.repository.BatchModuleRepository;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.ModuleRepository;
import com.Backend.Backend.repository.TeacherRepository;
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
    private final TeacherRepository teacherRepository;

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
                .teacher(findTeacherOrNull(request.getTeacherId()))
                .build();

        applyDeliveryPattern(batchModule, request);

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
        batchModule.setTeacher(findTeacherOrNull(request.getTeacherId()));

        applyDeliveryPattern(batchModule, request);

        return mapToDto(batchModuleRepository.save(batchModule));
    }

    // Unassign a module from a batch
    @Transactional
    public void delete(UUID batchModuleId) {
        batchModuleRepository.delete(findOrThrow(batchModuleId));
    }

    /**
     * Copies the weekly delivery pattern across, keeping each entity default when a field is omitted.
     * The generator reads exactly these numbers when it works out what it has to place.
     */
    private void applyDeliveryPattern(BatchModuleEntity batchModule, BatchModuleRequestDto request) {
        if (request.getLectureSessionsPerWeek() != null) {
            batchModule.setLectureSessionsPerWeek(request.getLectureSessionsPerWeek());
        }
        if (request.getLectureDurationMinutes() != null) {
            batchModule.setLectureDurationMinutes(request.getLectureDurationMinutes());
        }
        if (request.getTutorialSessionsPerWeek() != null) {
            batchModule.setTutorialSessionsPerWeek(request.getTutorialSessionsPerWeek());
        }
        if (request.getTutorialDurationMinutes() != null) {
            batchModule.setTutorialDurationMinutes(request.getTutorialDurationMinutes());
        }
        if (request.getWorkshopSessionsPerWeek() != null) {
            batchModule.setWorkshopSessionsPerWeek(request.getWorkshopSessionsPerWeek());
        }
        if (request.getWorkshopDurationMinutes() != null) {
            batchModule.setWorkshopDurationMinutes(request.getWorkshopDurationMinutes());
        }
        if (request.getSplitTutorialByGroup() != null) {
            batchModule.setSplitTutorialByGroup(request.getSplitTutorialByGroup());
        }
        if (request.getSplitWorkshopByGroup() != null) {
            batchModule.setSplitWorkshopByGroup(request.getSplitWorkshopByGroup());
        }
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

    // Optional, the generator picks a lecturer itself when none is named
    private TeacherEntity findTeacherOrNull(UUID teacherId) {
        if (teacherId == null) {
            return null;
        }
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
    }

    // Entity to response DTO
    private BatchModuleResponseDto mapToDto(BatchModuleEntity batchModule) {
        BatchEntity batch = batchModule.getBatch();
        ModuleEntity module = batchModule.getModule();
        TeacherEntity teacher = batchModule.getTeacher();

        int groups = batch != null && batch.getStudentGroups() != null
                ? Math.max(batch.getStudentGroups().size(), 1)
                : 1;

        int tutorialCopies = Boolean.TRUE.equals(batchModule.getSplitTutorialByGroup()) ? groups : 1;
        int workshopCopies = Boolean.TRUE.equals(batchModule.getSplitWorkshopByGroup()) ? groups : 1;

        // One student attends each type once, however many group copies the timetable holds
        double contactMinutes = batchModule.getLectureSessionsPerWeek() * batchModule.getLectureDurationMinutes()
                + batchModule.getTutorialSessionsPerWeek() * batchModule.getTutorialDurationMinutes()
                + batchModule.getWorkshopSessionsPerWeek() * batchModule.getWorkshopDurationMinutes();

        int sessionCount = batchModule.getLectureSessionsPerWeek()
                + batchModule.getTutorialSessionsPerWeek() * tutorialCopies
                + batchModule.getWorkshopSessionsPerWeek() * workshopCopies;

        return BatchModuleResponseDto.builder()
                .batchModuleId(batchModule.getBatchModuleId())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .moduleId(module != null ? module.getModuleId() : null)
                .moduleCode(module != null ? module.getModuleCode() : null)
                .moduleName(module != null ? module.getModuleName() : null)
                .credits(module != null ? module.getCredits() : null)
                .teacherId(teacher != null ? teacher.getTeacherId() : null)
                .teacherName(teacher != null && teacher.getUser() != null ? teacher.getUser().getFullName() : null)
                .lectureSessionsPerWeek(batchModule.getLectureSessionsPerWeek())
                .lectureDurationMinutes(batchModule.getLectureDurationMinutes())
                .tutorialSessionsPerWeek(batchModule.getTutorialSessionsPerWeek())
                .tutorialDurationMinutes(batchModule.getTutorialDurationMinutes())
                .workshopSessionsPerWeek(batchModule.getWorkshopSessionsPerWeek())
                .workshopDurationMinutes(batchModule.getWorkshopDurationMinutes())
                .splitTutorialByGroup(batchModule.getSplitTutorialByGroup())
                .splitWorkshopByGroup(batchModule.getSplitWorkshopByGroup())
                .weeklyContactHours(Math.round(contactMinutes / 60.0 * 100.0) / 100.0)
                .weeklySessionCount(sessionCount)
                .createdAt(batchModule.getCreatedAt())
                .build();
    }
}
