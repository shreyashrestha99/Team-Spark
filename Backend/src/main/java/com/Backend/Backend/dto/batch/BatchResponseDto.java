package com.Backend.Backend.dto.batch;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponseDto {

    private UUID batchId;
    private String batchName;
    private Integer yearOfStudy;
    private Integer semester;
    private Boolean isActive;

    private UUID programmeId;
    private String programmeName;
    private String programmeCode;

    /** Human-friendly label for dropdowns, e.g. "BSc (Hons) Computing — Year 2 / Sem 4". */
    private String label;
}
