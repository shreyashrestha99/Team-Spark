package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.portal.MyExamSeatDto;
import com.Backend.Backend.dto.portal.StudentDashboardDto;
import com.Backend.Backend.dto.timetable.TimetableSessionResponseDto;
import com.Backend.Backend.service.StudentPortalService;
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
 * The student's own view. Every endpoint is scoped to the signed-in account, so there
 * is no id to tamper with and no way to read another student's timetable or seat.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    // Landing view: today, this week, and the next exam seat
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<StudentDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StudentDashboardDto dashboard = studentPortalService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard fetched successfully", dashboard));
    }

    // My routine, defaulting to the current week
    @GetMapping("/routine")
    public ResponseEntity<ApiResponseDto<List<TimetableSessionResponseDto>>> getRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<TimetableSessionResponseDto> routine =
                studentPortalService.getMyRoutine(userDetails.getUsername(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponseDto.success("Routine fetched successfully", routine));
    }

    // My exams, each with the hall and desk I was given
    @GetMapping("/exams")
    public ResponseEntity<ApiResponseDto<List<MyExamSeatDto>>> getExams(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<MyExamSeatDto> exams = studentPortalService.getMyExams(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Exams fetched successfully", exams));
    }
}
