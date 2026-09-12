package com.Backend.Backend.config;

import com.Backend.Backend.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 401 — bad login credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    // 400 — invalid input value
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    // 403 — action not allowed
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    // 400 — bean validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponseDto<Map<String, String>> response = ApiResponseDto.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 500 — catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"));
    }
}
