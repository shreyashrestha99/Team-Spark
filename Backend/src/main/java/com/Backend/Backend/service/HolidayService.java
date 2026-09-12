package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.holiday.HolidayRequestDto;
import com.Backend.Backend.dto.holiday.HolidayResponseDto;
import com.Backend.Backend.entity.HolidayEntity;
import com.Backend.Backend.repository.HolidayRepository;
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
public class HolidayService {

    private final HolidayRepository holidayRepository;

    // Mark a date the generator must skip
    @Transactional
    public HolidayResponseDto create(HolidayRequestDto request) {
        if (holidayRepository.existsByHolidayDate(request.getHolidayDate())) {
            throw new IllegalArgumentException("A holiday is already recorded on " + request.getHolidayDate());
        }

        HolidayEntity holiday = HolidayEntity.builder()
                .holidayDate(request.getHolidayDate())
                .holidayName(request.getHolidayName().trim())
                .build();

        return mapToDto(holidayRepository.save(holiday));
    }

    // Paged list for the admin table
    @Transactional(readOnly = true)
    public PageResponseDto<HolidayResponseDto> getAll(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<HolidayEntity> result = holidayRepository.findAll(pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Every holiday, earliest first
    @Transactional(readOnly = true)
    public List<HolidayResponseDto> getUpcoming() {
        return holidayRepository.findAllByOrderByHolidayDateAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one holiday by id
    @Transactional(readOnly = true)
    public HolidayResponseDto getById(UUID holidayId) {
        return mapToDto(findOrThrow(holidayId));
    }

    // Update a holiday
    @Transactional
    public HolidayResponseDto update(UUID holidayId, HolidayRequestDto request) {
        HolidayEntity holiday = findOrThrow(holidayId);

        if (holidayRepository.existsByHolidayDateAndHolidayIdNot(request.getHolidayDate(), holidayId)) {
            throw new IllegalArgumentException("A holiday is already recorded on " + request.getHolidayDate());
        }

        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setHolidayName(request.getHolidayName().trim());

        return mapToDto(holidayRepository.save(holiday));
    }

    // Remove a holiday
    @Transactional
    public void delete(UUID holidayId) {
        holidayRepository.delete(findOrThrow(holidayId));
    }

    private HolidayEntity findOrThrow(UUID holidayId) {
        return holidayRepository.findById(holidayId)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found: " + holidayId));
    }

    // Entity to response DTO
    private HolidayResponseDto mapToDto(HolidayEntity holiday) {
        return HolidayResponseDto.builder()
                .holidayId(holiday.getHolidayId())
                .holidayDate(holiday.getHolidayDate())
                .holidayName(holiday.getHolidayName())
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .build();
    }
}
