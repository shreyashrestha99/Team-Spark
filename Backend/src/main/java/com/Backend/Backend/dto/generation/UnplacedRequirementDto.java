package com.Backend.Backend.dto.generation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnplacedRequirementDto {

    private String moduleCode;
    private String moduleName;

    // LECTURE, TUTORIAL or WORKSHOP
    private String sessionType;

    // Null for a whole-batch lecture
    private String groupName;

    private Integer durationMinutes;

    // Plain-language cause, e.g. which resource ran out
    private String reason;
}
