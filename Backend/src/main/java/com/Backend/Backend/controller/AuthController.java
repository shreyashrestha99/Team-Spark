package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.AuthResponseDto;
import com.Backend.Backend.dto.LoginRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Authenticate and issue JWT
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success("Login successful", response));
    }

    // Clear the security context
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponseDto.success("Logged out successfully"));
    }

    // Return the logged-in user
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User is not authenticated"));
        }
        UserResponseDto user = authService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("User profile fetched successfully", user));
    }
}
