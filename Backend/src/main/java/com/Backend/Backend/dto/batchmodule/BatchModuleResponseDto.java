package com.Backend.Backend.dto.batchmodule;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchModuleResponseDto {

    private UUID batchModuleId;

    private UUID batchId;
    private String batchName;

    private UUID moduleId;
    private String moduleCode;
    private String moduleName;
    private Integer credits;

    private LocalDateTime createdAt;
}
