package com.Backend.Backend.dto.invigilator;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorRequestDto {

    @NotNull(message = "Exam is required")
    private UUID examId;

    @NotNull(message = "User is required")
    private UUID userId;

    // Defaults to INVIGILATOR when omitted
    @Size(max = 50, message = "Role must be under 50 characters")
    private String role;
}
