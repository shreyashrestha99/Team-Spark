package com.Backend.Backend.dto.generation;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationRunResponseDto {

    private UUID runId;
    private UUID batchId;
    private String batchName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;

    private Integer requirementsTotal;
    private Integer requirementsPlaced;
    private Integer penaltyScore;
    private Integer sessionsCreated;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
