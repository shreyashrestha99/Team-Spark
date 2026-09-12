package com.Backend.Backend.dto.programme;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammeRequestDto {

    @NotBlank(message = "Programme name is required")
    @Size(max = 150, message = "Programme name must be under 150 characters")
    private String programmeName;

    @NotBlank(message = "Programme code is required")
    @Size(max = 50, message = "Programme code must be under 50 characters")
    private String programmeCode;

    @NotNull(message = "Duration in years is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 10, message = "Duration must be 10 years or less")
    private Integer durationYears;

    // Defaults to true when omitted
    private Boolean isActive;
}
