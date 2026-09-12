package com.Backend.Backend.dto.batchmodule;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchModuleRequestDto {

    @NotNull(message = "Batch is required")
    private UUID batchId;

    @NotNull(message = "Module is required")
    private UUID moduleId;
}
