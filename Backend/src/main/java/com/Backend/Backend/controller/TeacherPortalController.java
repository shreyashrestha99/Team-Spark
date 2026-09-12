package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.portal.InvigilationDutyDto;
import com.Backend.Backend.dto.portal.TeacherDashboardDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.service.TeacherPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * The lecturer's own view: what they teach and what they must invigilate.
 * Scoped to the signed-in account, exactly like the student portal.
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPortalController {

    private final TeacherPortalService teacherPortalService;

    // Landing view: today's classes, weekly load, next duty
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<TeacherDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TeacherDashboardDto dashboard = teacherPortalService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard fetched successfully", dashboard));
    }

    // My teaching routine, defaulting to the current week
    @GetMapping("/routine")
    public ResponseEntity<ApiResponseDto<List<TimetableSessionResponseDto>>> getRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<TimetableSessionResponseDto> routine =
                teacherPortalService.getMyRoutine(userDetails.getUsername(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponseDto.success("Routine fetched successfully", routine));
    }

    // Exams I am rostered to invigilate
    @GetMapping("/invigilation")
    public ResponseEntity<ApiResponseDto<List<InvigilationDutyDto>>> getInvigilation(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<InvigilationDutyDto> duties = teacherPortalService.getMyDuties(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Invigilation duties fetched successfully", duties));
    }
}
