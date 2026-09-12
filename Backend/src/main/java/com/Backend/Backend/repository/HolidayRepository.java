package com.Backend.Backend.repository;

import com.Backend.Backend.entity.HolidayEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayEntity, UUID> {

    List<HolidayEntity> findAllByOrderByHolidayDateAsc();

    List<HolidayEntity> findAllByHolidayDateBetween(LocalDate fromDate, LocalDate toDate);

    boolean existsByHolidayDate(LocalDate holidayDate);

    Page<HolidayEntity> findAll(Pageable pageable);

    boolean existsByHolidayDateAndHolidayIdNot(LocalDate holidayDate, UUID holidayId);
}
