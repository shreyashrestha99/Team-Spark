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

    private UUID teacherId;
    private String teacherName;

    private Integer lectureSessionsPerWeek;
    private Integer lectureDurationMinutes;
    private Integer tutorialSessionsPerWeek;
    private Integer tutorialDurationMinutes;
    private Integer workshopSessionsPerWeek;
    private Integer workshopDurationMinutes;
    private Boolean splitTutorialByGroup;
    private Boolean splitWorkshopByGroup;

    /** Contact hours one student sits through each week under this pattern. */
    private double weeklyContactHours;

    /** Sessions the generator must place each week, groups already multiplied out. */
    private int weeklySessionCount;

    private LocalDateTime createdAt;
}
