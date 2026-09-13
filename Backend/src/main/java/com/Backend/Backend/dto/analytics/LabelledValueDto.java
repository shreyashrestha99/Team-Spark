package com.Backend.Backend.dto.analytics;

import lombok.*;

/** One labelled magnitude, the shape every chart on the dashboard consumes. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelledValueDto {

    private String label;
    private double value;

    // Optional context, e.g. "of 54 slots" beside a utilisation percentage
    private String detail;
}
