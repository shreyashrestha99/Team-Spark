package com.Backend.Backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be under 100 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be under 20 characters")
    private String phoneNumber;

    // Unchanged when omitted
    private Boolean isActive;

    // Only re-hashed when supplied
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Student-specific fields
    private String studentNumber;
    private String registrationNumber;
    private String status;
    private UUID batchId;

    // Teacher & staff fields
    private String department;
    private String designation;
}
