package com.Backend.Backend.dto.user;

import lombok.*;

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
}
