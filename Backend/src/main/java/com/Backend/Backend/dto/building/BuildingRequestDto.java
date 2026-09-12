package com.Backend.Backend.dto.building;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingRequestDto {

    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name must be under 100 characters")
    private String buildingName;

    @NotBlank(message = "Building code is required")
    @Size(max = 20, message = "Building code must be under 20 characters")
    private String buildingCode;

    @Min(value = 1, message = "Floors must be at least 1")
    @Max(value = 100, message = "Floors must be 100 or less")
    private Integer floors;

    // Defaults to true when omitted
    private Boolean isActive;
}
