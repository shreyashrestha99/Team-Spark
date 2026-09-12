package com.Backend.Backend.dto.studentgroup;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupResponseDto {

    private UUID groupId;
    private UUID batchId;
    private String batchName;
    private String groupName;

    // How many students sit in this group
    private int studentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
