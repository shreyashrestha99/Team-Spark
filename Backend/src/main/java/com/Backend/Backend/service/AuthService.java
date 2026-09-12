package com.Backend.Backend.service;

import com.Backend.Backend.config.JwtService;
import com.Backend.Backend.dto.AuthResponseDto;
import com.Backend.Backend.dto.LoginRequestDto;
import com.Backend.Backend.dto.user.UserResponseDto;
import com.Backend.Backend.entity.UserEntity;
import com.Backend.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthResponseDto login(LoginRequestDto request) {
        UserEntity user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("User account is deactivated");
        }

        return generateAuthResponse(user);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public UserResponseDto getCurrentUserProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return userService.mapToUserResponseDto(user);
    }

    private AuthResponseDto generateAuthResponse(UserEntity user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("fullName", user.getFullName());
        extraClaims.put("role", user.getRole().getRoleName().name());

        String jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponseDto.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(userService.mapToUserResponseDto(user))
                .build();
    }
}
