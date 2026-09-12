package com.Backend.Backend.repository;

import com.Backend.Backend.entity.TimeSlotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {

    List<TimeSlotEntity> findAllByOrderByDayOfWeekAscPeriodNumberAsc();

    List<TimeSlotEntity> findAllByIsTeachingSlotTrueOrderByDayOfWeekAscPeriodNumberAsc();

    Optional<TimeSlotEntity> findByDayOfWeekAndPeriodNumber(DayOfWeek dayOfWeek, Integer periodNumber);

    boolean existsByDayOfWeekAndPeriodNumber(DayOfWeek dayOfWeek, Integer periodNumber);

    Page<TimeSlotEntity> findAll(Pageable pageable);
}
