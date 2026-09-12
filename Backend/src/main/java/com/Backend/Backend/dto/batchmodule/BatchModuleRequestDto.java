package com.Backend.Backend.dto.batchmodule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    // Optional. Left null the generator picks the lightest loaded lecturer.
    private UUID teacherId;

    // Delivery pattern. Every field falls back to the usual lecture plus tutorial plus workshop week.
    @Min(value = 0, message = "Lecture sessions cannot be negative")
    @Max(value = 10, message = "Lecture sessions must be 10 or less")
    private Integer lectureSessionsPerWeek;

    @Min(value = 30, message = "Lecture duration must be at least 30 minutes")
    @Max(value = 480, message = "Lecture duration must be 480 minutes or less")
    private Integer lectureDurationMinutes;

    @Min(value = 0, message = "Tutorial sessions cannot be negative")
    @Max(value = 10, message = "Tutorial sessions must be 10 or less")
    private Integer tutorialSessionsPerWeek;

    @Min(value = 30, message = "Tutorial duration must be at least 30 minutes")
    @Max(value = 480, message = "Tutorial duration must be 480 minutes or less")
    private Integer tutorialDurationMinutes;

    @Min(value = 0, message = "Workshop sessions cannot be negative")
    @Max(value = 10, message = "Workshop sessions must be 10 or less")
    private Integer workshopSessionsPerWeek;

    @Min(value = 30, message = "Workshop duration must be at least 30 minutes")
    @Max(value = 480, message = "Workshop duration must be 480 minutes or less")
    private Integer workshopDurationMinutes;

    // Whether tutorials and workshops run once per group rather than once for the whole cohort
    private Boolean splitTutorialByGroup;
    private Boolean splitWorkshopByGroup;
}
