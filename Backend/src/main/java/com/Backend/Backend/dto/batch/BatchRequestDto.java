package com.Backend.Backend.dto.batch;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRequestDto {

    @NotNull(message = "Programme is required")
    private UUID programmeId;

    @NotBlank(message = "Batch name is required")
    @Size(max = 100, message = "Batch name must be under 100 characters")
    private String batchName;

    // Year the cohort started, e.g. 2025
    @Min(value = 2000, message = "Intake year must be 2000 or later")
    @Max(value = 2100, message = "Intake year must be 2100 or earlier")
    private Integer intakeYear;

    // MORNING or DAY, restricts the generator to that half of the grid
    @Size(max = 30, message = "Shift must be under 30 characters")
    private String shift;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 10, message = "Year of study must be 10 or less")
    private Integer yearOfStudy;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 12, message = "Semester must be 12 or less")
    private Integer semester;

    private LocalDate startDate;
    private LocalDate endDate;

    // Defaults to true when omitted
    private Boolean isActive;
}
