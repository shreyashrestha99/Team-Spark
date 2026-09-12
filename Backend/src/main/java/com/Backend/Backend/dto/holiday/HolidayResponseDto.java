package com.Backend.Backend.dto.holiday;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayResponseDto {

    private UUID holidayId;
    private LocalDate holidayDate;
    private String holidayName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
