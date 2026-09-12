package com.Backend.Backend.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID userId;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Student specific
    private UUID studentId;
    private String studentNumber;
    private String registrationNumber;
    private String studentStatus;
    private UUID batchId;
    private String batchName;
    private String programmeName;

    // Teacher specific
    private UUID teacherId;
    private String teacherDepartment;
    private String teacherDesignation;

    // Staff specific
    private UUID staffId;
    private String staffDepartment;
    private String staffDesignation;
}
