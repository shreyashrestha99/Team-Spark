package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.timeslot.TimeSlotRequestDto;
import com.Backend.Backend.dto.timeslot.TimeSlotResponseDto;
import com.Backend.Backend.entity.TimeSlotEntity;
import com.Backend.Backend.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    // Add one cell to the weekly grid
    @Transactional
    public TimeSlotResponseDto create(TimeSlotRequestDto request) {
        validateTimes(request);

        if (timeSlotRepository.existsByDayOfWeekAndPeriodNumber(request.getDayOfWeek(), request.getPeriodNumber())) {
            throw new IllegalArgumentException(
                    "Period " + request.getPeriodNumber() + " already exists on " + request.getDayOfWeek()
            );
        }

        TimeSlotEntity slot = TimeSlotEntity.builder()
                .dayOfWeek(request.getDayOfWeek())
                .periodNumber(request.getPeriodNumber())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isTeachingSlot(request.getIsTeachingSlot() == null || request.getIsTeachingSlot())
                .build();

        return mapToDto(timeSlotRepository.save(slot));
    }

    // Paged list for the admin table
    @Transactional(readOnly = true)
    public PageResponseDto<TimeSlotResponseDto> getAll(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TimeSlotEntity> result = timeSlotRepository.findAll(pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // The whole grid, ordered for rendering
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDto> getGrid() {
        return timeSlotRepository.findAllByOrderByDayOfWeekAscPeriodNumberAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Only the cells a session may occupy
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDto> getTeachingSlots() {
        return timeSlotRepository.findAllByIsTeachingSlotTrueOrderByDayOfWeekAscPeriodNumberAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one slot by id
    @Transactional(readOnly = true)
    public TimeSlotResponseDto getById(UUID slotId) {
        return mapToDto(findOrThrow(slotId));
    }

    // Update a grid cell
    @Transactional
    public TimeSlotResponseDto update(UUID slotId, TimeSlotRequestDto request) {
        TimeSlotEntity slot = findOrThrow(slotId);
        validateTimes(request);

        boolean movedCell = !slot.getDayOfWeek().equals(request.getDayOfWeek())
                || !slot.getPeriodNumber().equals(request.getPeriodNumber());

        if (movedCell && timeSlotRepository.existsByDayOfWeekAndPeriodNumber(
                request.getDayOfWeek(), request.getPeriodNumber())) {
            throw new IllegalArgumentException(
                    "Period " + request.getPeriodNumber() + " already exists on " + request.getDayOfWeek()
            );
        }

        slot.setDayOfWeek(request.getDayOfWeek());
        slot.setPeriodNumber(request.getPeriodNumber());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        if (request.getIsTeachingSlot() != null) {
            slot.setIsTeachingSlot(request.getIsTeachingSlot());
        }

        return mapToDto(timeSlotRepository.save(slot));
    }

    // Remove a grid cell
    @Transactional
    public void delete(UUID slotId) {
        timeSlotRepository.delete(findOrThrow(slotId));
    }

    // End time must follow start time
    private void validateTimes(TimeSlotRequestDto request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private TimeSlotEntity findOrThrow(UUID slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found: " + slotId));
    }

    // Entity to response DTO
    private TimeSlotResponseDto mapToDto(TimeSlotEntity slot) {
        return TimeSlotResponseDto.builder()
                .slotId(slot.getSlotId())
                .dayOfWeek(slot.getDayOfWeek())
                .periodNumber(slot.getPeriodNumber())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isTeachingSlot(slot.getIsTeachingSlot())
                .durationMinutes(Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}
