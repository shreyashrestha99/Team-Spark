package com.Backend.Backend.dto.generation;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateRoutineRequestDto {

    @NotNull(message = "Batch is required")
    private UUID batchId;

    @NotNull(message = "Start date is required")
    private LocalDate fromDate;

    @NotNull(message = "End date is required")
    private LocalDate toDate;

    // Wipes the batch's existing sessions in the window before generating. Defaults to true.
    private Boolean replaceExisting;

    // Runs the local search pass that trims idle gaps. Defaults to true.
    private Boolean optimise;
}
