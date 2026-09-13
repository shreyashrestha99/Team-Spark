package com.Backend.Backend.dto.analytics;

import lombok.*;

/** Weekly teaching load for one lecturer, measured against their contracted ceiling. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherLoadDto {

    private String name;
    private String department;

    private double weeklyHours;
    private int maxWeeklyHours;

    // weeklyHours as a percentage of the ceiling
    private double utilisationPct;
    private boolean overloaded;
}
