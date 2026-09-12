package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.invigilator.InvigilatorRequestDto;
import com.Backend.Backend.dto.invigilator.InvigilatorResponseDto;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.InvigilatorEntity;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.InvigilatorRepository;
import com.Backend.Backend.repository.UserRepository;
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
public class InvigilatorService {

    private final InvigilatorRepository invigilatorRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    // Assign an invigilator to an exam
    @Transactional
    public InvigilatorResponseDto create(InvigilatorRequestDto request) {
        if (invigilatorRepository.existsByExam_ExamIdAndUser_UserId(request.getExamId(), request.getUserId())) {
            throw new IllegalArgumentException("This person is already assigned to the exam");
        }

        ExamEntity exam = findExamOrThrow(request.getExamId());
        UserEntity user = findUserOrThrow(request.getUserId());

        rejectIfDutyClashes(exam, user);

        InvigilatorEntity invigilator = InvigilatorEntity.builder()
                .exam(exam)
                .user(user)
                .role(hasText(request.getRole()) ? request.getRole().trim() : "INVIGILATOR")
                .build();

        return mapToDto(invigilatorRepository.save(invigilator));
    }

    // Paged list with exam and user filters
    @Transactional(readOnly = true)
    public PageResponseDto<InvigilatorResponseDto> getAll(
            UUID examId,
            UUID userId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InvigilatorEntity> result = invigilatorRepository.searchInvigilators(examId, userId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Duty roster for one exam
    @Transactional(readOnly = true)
    public List<InvigilatorResponseDto> getByExam(UUID examId) {
        return invigilatorRepository.findAllByExam_ExamId(examId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one assignment by id
    @Transactional(readOnly = true)
    public InvigilatorResponseDto getById(UUID invigilatorId) {
        return mapToDto(findOrThrow(invigilatorId));
    }

    // Update an assignment
    @Transactional
    public InvigilatorResponseDto update(UUID invigilatorId, InvigilatorRequestDto request) {
        InvigilatorEntity invigilator = findOrThrow(invigilatorId);

        boolean pairChanged = !invigilator.getExam().getExamId().equals(request.getExamId())
                || !invigilator.getUser().getUserId().equals(request.getUserId());

        if (pairChanged && invigilatorRepository.existsByExam_ExamIdAndUser_UserId(
                request.getExamId(), request.getUserId())) {
            throw new IllegalArgumentException("This person is already assigned to the exam");
        }

        ExamEntity exam = findExamOrThrow(request.getExamId());
        UserEntity user = findUserOrThrow(request.getUserId());

        if (pairChanged) {
            rejectIfDutyClashes(exam, user);
        }

        invigilator.setExam(exam);
        invigilator.setUser(user);
        if (hasText(request.getRole())) {
            invigilator.setRole(request.getRole().trim());
        }

        return mapToDto(invigilatorRepository.save(invigilator));
    }

    // Remove an invigilator from duty
    @Transactional
    public void delete(UUID invigilatorId) {
        invigilatorRepository.delete(findOrThrow(invigilatorId));
    }

    // Nobody can invigilate two overlapping exams
    private void rejectIfDutyClashes(ExamEntity exam, UserEntity user) {
        List<InvigilatorEntity> clashes = invigilatorRepository.findDutyClashes(
                user.getUserId(),
                exam.getExamId(),
                exam.getExamDate(),
                exam.getStartTime(),
                exam.getEndTime()
        );

        if (!clashes.isEmpty()) {
            ExamEntity other = clashes.get(0).getExam();
            throw new IllegalArgumentException(
                    user.getFullName() + " is already on duty for "
                            + other.getModule().getModuleCode() + " on " + other.getExamDate()
                            + " at " + other.getStartTime() + "–" + other.getEndTime()
            );
        }
    }

    private InvigilatorEntity findOrThrow(UUID invigilatorId) {
        return invigilatorRepository.findById(invigilatorId)
                .orElseThrow(() -> new IllegalArgumentException("Invigilator not found: " + invigilatorId));
    }

    private ExamEntity findExamOrThrow(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    }

    private UserEntity findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Entity to response DTO
    private InvigilatorResponseDto mapToDto(InvigilatorEntity invigilator) {
        ExamEntity exam = invigilator.getExam();
        UserEntity user = invigilator.getUser();

        return InvigilatorResponseDto.builder()
                .invigilatorId(invigilator.getInvigilatorId())
                .examId(exam != null ? exam.getExamId() : null)
                .moduleCode(exam != null && exam.getModule() != null ? exam.getModule().getModuleCode() : null)
                .batchName(exam != null && exam.getBatch() != null ? exam.getBatch().getBatchName() : null)
                .examDate(exam != null ? exam.getExamDate() : null)
                .startTime(exam != null ? exam.getStartTime() : null)
                .endTime(exam != null ? exam.getEndTime() : null)
                .userId(user != null ? user.getUserId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .userRole(user != null && user.getRole() != null ? user.getRole().getRoleName().name() : null)
                .role(invigilator.getRole())
                .createdAt(invigilator.getCreatedAt())
                .updatedAt(invigilator.getUpdatedAt())
                .build();
    }
}
