package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.availability.TeacherAvailabilityRequestDto;
import com.Backend.Backend.dto.availability.TeacherAvailabilityResponseDto;
import com.Backend.Backend.entity.TeacherAvailabilityEntity;
import com.Backend.Backend.entity.TeacherEntity;
import com.Backend.Backend.repository.TeacherAvailabilityRepository;
import com.Backend.Backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherAvailabilityService {

    private static final Set<String> VALID_TYPES = Set.of("UNAVAILABLE", "PREFERRED");

    private final TeacherAvailabilityRepository availabilityRepository;
    private final TeacherRepository teacherRepository;

    // Record a window a lecturer cannot or would rather teach
    @Transactional
    public TeacherAvailabilityResponseDto create(TeacherAvailabilityRequestDto request) {
        validateTimes(request);

        TeacherAvailabilityEntity availability = TeacherAvailabilityEntity.builder()
                .teacher(findTeacherOrThrow(request.getTeacherId()))
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .availabilityType(normaliseType(request.getAvailabilityType()))
                .note(trimOrNull(request.getNote()))
                .build();

        return mapToDto(availabilityRepository.save(availability));
    }

    // Paged list with lecturer and type filters
    @Transactional(readOnly = true)
    public PageResponseDto<TeacherAvailabilityResponseDto> getAll(
            UUID teacherId,
            String type,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeacherAvailabilityEntity> result = availabilityRepository.searchAvailability(
                teacherId, type == null ? "" : type.trim(), pageable
        );

        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Every window for one lecturer
    @Transactional(readOnly = true)
    public List<TeacherAvailabilityResponseDto> getByTeacher(UUID teacherId) {
        return availabilityRepository.findAllByTeacher_TeacherId(teacherId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one window by id
    @Transactional(readOnly = true)
    public TeacherAvailabilityResponseDto getById(UUID availabilityId) {
        return mapToDto(findOrThrow(availabilityId));
    }

    // Update a window
    @Transactional
    public TeacherAvailabilityResponseDto update(UUID availabilityId, TeacherAvailabilityRequestDto request) {
        TeacherAvailabilityEntity availability = findOrThrow(availabilityId);
        validateTimes(request);

        availability.setTeacher(findTeacherOrThrow(request.getTeacherId()));
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setAvailabilityType(normaliseType(request.getAvailabilityType()));
        availability.setNote(trimOrNull(request.getNote()));

        return mapToDto(availabilityRepository.save(availability));
    }

    // Remove a window
    @Transactional
    public void delete(UUID availabilityId) {
        availabilityRepository.delete(findOrThrow(availabilityId));
    }

    // End time must follow start time
    private void validateTimes(TeacherAvailabilityRequestDto request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    // Only UNAVAILABLE and PREFERRED mean anything to the generator
    private String normaliseType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "UNAVAILABLE";
        }

        String upper = type.trim().toUpperCase();
        if (!VALID_TYPES.contains(upper)) {
            throw new IllegalArgumentException("Availability type must be UNAVAILABLE or PREFERRED");
        }

        return upper;
    }

    private TeacherAvailabilityEntity findOrThrow(UUID availabilityId) {
        return availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found: " + availabilityId));
    }

    private TeacherEntity findTeacherOrThrow(UUID teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Entity to response DTO
    private TeacherAvailabilityResponseDto mapToDto(TeacherAvailabilityEntity availability) {
        TeacherEntity teacher = availability.getTeacher();

        return TeacherAvailabilityResponseDto.builder()
                .availabilityId(availability.getAvailabilityId())
                .teacherId(teacher != null ? teacher.getTeacherId() : null)
                .teacherName(teacher != null && teacher.getUser() != null ? teacher.getUser().getFullName() : null)
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .availabilityType(availability.getAvailabilityType())
                .note(availability.getNote())
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
    }
}
