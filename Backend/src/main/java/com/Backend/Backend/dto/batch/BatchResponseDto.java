package com.Backend.Backend.dto.batch;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponseDto {

    private UUID batchId;
    private String batchName;
    private Integer intakeYear;
    private String shift;
    private Integer yearOfStudy;
    private Integer semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    private UUID programmeId;
    private String programmeName;
    private String programmeCode;

    // How many students are in this batch
    private int studentCount;

    // Tutorial and workshop groups this batch is split into
    private int groupCount;

    /** Human-friendly label for dropdowns, e.g. "BSc (Hons) Computing — Year 2 / Sem 4". */
    private String label;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
