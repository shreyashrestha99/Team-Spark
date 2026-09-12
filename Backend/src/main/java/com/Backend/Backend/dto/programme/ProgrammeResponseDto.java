package com.Backend.Backend.dto.programme;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammeResponseDto {

    private UUID programmeId;
    private String programmeName;
    private String programmeCode;
    private Integer durationYears;
    private Boolean isActive;

    // How many batches reference this programme
    private int batchCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
