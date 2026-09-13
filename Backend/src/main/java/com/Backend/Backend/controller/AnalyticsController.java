package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.analytics.AdminAnalyticsDto;
import com.Backend.Backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Institution-wide figures for the dashboard
    @GetMapping
    public ResponseEntity<ApiResponseDto<AdminAnalyticsDto>> getAnalytics() {
        AdminAnalyticsDto analytics = analyticsService.getAdminAnalytics();
        return ResponseEntity.ok(ApiResponseDto.success("Analytics fetched successfully", analytics));
    }
}
