package com.Backend.Backend.dto.module;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleResponseDto {

    private UUID moduleId;
    private String moduleCode;
    private String moduleName;
    private Integer credits;
    private Integer yearOfStudy;
    private Integer semester;
    private Boolean isActive;

    // How many batches study this module
    private int batchCount;

    /** Human-friendly label for dropdowns, e.g. "CS5001 — Software Engineering". */
    private String label;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
