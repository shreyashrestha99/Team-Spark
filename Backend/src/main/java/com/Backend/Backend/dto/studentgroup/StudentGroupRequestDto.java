package com.Backend.Backend.dto.studentgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupRequestDto {

    @NotNull(message = "Batch is required")
    private UUID batchId;

    @NotBlank(message = "Group name is required")
    @Size(max = 50, message = "Group name must be under 50 characters")
    private String groupName;
}
