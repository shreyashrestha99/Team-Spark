package com.Backend.Backend.controller;

import com.Backend.Backend.dto.ApiResponseDto;
import com.Backend.Backend.dto.seating.SeatingPlanDto;
import com.Backend.Backend.service.ExamSeatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/exams/{examId}/seating")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExamSeatingController {

    private final ExamSeatingService examSeatingService;

    // Pick halls and seat every candidate, replacing any existing allocation
    @PostMapping("/auto-allocate")
    public ResponseEntity<ApiResponseDto<SeatingPlanDto>> autoAllocate(@PathVariable UUID examId) {
        SeatingPlanDto plan = examSeatingService.autoAllocate(examId);

        String message = plan.getUnseatedStudents() == 0
                ? "Seated " + plan.getSeatedStudents() + " candidate(s) across "
                        + plan.getRooms().size() + " hall(s)"
                : plan.getUnseatedStudents() + " candidate(s) could not be seated";

        return ResponseEntity.ok(ApiResponseDto.success(message, plan));
    }

    // The saved plan, every desk in every hall
    @GetMapping
    public ResponseEntity<ApiResponseDto<SeatingPlanDto>> getPlan(@PathVariable UUID examId) {
        SeatingPlanDto plan = examSeatingService.getPlan(examId);
        return ResponseEntity.ok(ApiResponseDto.success("Seating plan fetched successfully", plan));
    }

    // Release every hall and desk held by this exam
    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> clear(@PathVariable UUID examId) {
        examSeatingService.clearSeating(examId);
        return ResponseEntity.ok(ApiResponseDto.success("Seating cleared successfully"));
    }
}
