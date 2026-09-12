package com.Backend.Backend.dto.module;

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
public class ModuleRequestDto {

    @NotBlank(message = "Module code is required")
    @Size(max = 50, message = "Module code must be under 50 characters")
    private String moduleCode;

    @NotBlank(message = "Module name is required")
    @Size(max = 150, message = "Module name must be under 150 characters")
    private String moduleName;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 120, message = "Credits must be 120 or less")
    private Integer credits;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 10, message = "Year of study must be 10 or less")
    private Integer yearOfStudy;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 12, message = "Semester must be 12 or less")
    private Integer semester;

    // Defaults to true when omitted
    private Boolean isActive;
}
