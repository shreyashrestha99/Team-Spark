package com.Backend.Backend.dto.portal;

import lombok.*;

import java.util.UUID;

/** Who the signed-in person is, in the terms their own portal cares about. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProfileDto {

    private UUID userId;
    private String fullName;
    private String email;

    // Student side
    private UUID studentId;
    private String studentNumber;
    private String registrationNumber;
    private UUID batchId;
    private String batchName;
    private UUID groupId;
    private String groupName;
    private String programmeName;
    private Integer yearOfStudy;
    private Integer semester;

    // Lecturer side
    private UUID teacherId;
    private String department;
    private String designation;
    private Integer maxWeeklyHours;
}
