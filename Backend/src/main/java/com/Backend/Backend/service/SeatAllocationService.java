package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.seat.SeatAllocationRequestDto;
import com.Backend.Backend.dto.seat.SeatAllocationResponseDto;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.entity.SeatAllocationEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.ExamRoomRepository;
import com.Backend.Backend.repository.SeatAllocationRepository;
import com.Backend.Backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatAllocationService {

    private final SeatAllocationRepository seatAllocationRepository;
    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final StudentRepository studentRepository;

    // Seat a student for an exam
    @Transactional
    public SeatAllocationResponseDto create(SeatAllocationRequestDto request) {
        ExamEntity exam = findExamOrThrow(request.getExamId());
        ExamRoomEntity examRoom = findExamRoomOrThrow(request.getExamRoomId());
        StudentEntity student = findStudentOrThrow(request.getStudentId());
        String seat = request.getSeatNumber().trim();

        validateRoomBelongsToExam(exam, examRoom);
        validateStudentInExamBatch(exam, student);

        if (seatAllocationRepository.existsByExam_ExamIdAndStudent_StudentId(
                exam.getExamId(), student.getStudentId())) {
            throw new IllegalArgumentException("This student already has a seat for the exam");
        }

        if (seatAllocationRepository.existsByExamRoom_ExamRoomIdAndSeatNumberIgnoreCase(
                examRoom.getExamRoomId(), seat)) {
            throw new IllegalArgumentException("Seat '" + seat + "' is already taken in this room");
        }

        validateRoomHasSpace(examRoom);

        SeatAllocationEntity allocation = SeatAllocationEntity.builder()
                .exam(exam)
                .examRoom(examRoom)
                .student(student)
                .seatNumber(seat)
                .rowNumber(trimOrNull(request.getRowNumber()))
                .columnNumber(trimOrNull(request.getColumnNumber()))
                .build();

        return mapToDto(seatAllocationRepository.save(allocation));
    }

    // Paged list with filters
    @Transactional(readOnly = true)
    public PageResponseDto<SeatAllocationResponseDto> getAll(
            UUID examId,
            UUID examRoomId,
            UUID studentId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SeatAllocationEntity> result =
                seatAllocationRepository.searchAllocations(examId, examRoomId, studentId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Full seating plan for one exam
    @Transactional(readOnly = true)
    public List<SeatAllocationResponseDto> getByExam(UUID examId) {
        return seatAllocationRepository.findAllByExam_ExamId(examId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one allocation by id
    @Transactional(readOnly = true)
    public SeatAllocationResponseDto getById(UUID allocationId) {
        return mapToDto(findOrThrow(allocationId));
    }

    // Move a student to another seat
    @Transactional
    public SeatAllocationResponseDto update(UUID allocationId, SeatAllocationRequestDto request) {
        SeatAllocationEntity allocation = findOrThrow(allocationId);

        ExamEntity exam = findExamOrThrow(request.getExamId());
        ExamRoomEntity examRoom = findExamRoomOrThrow(request.getExamRoomId());
        StudentEntity student = findStudentOrThrow(request.getStudentId());
        String seat = request.getSeatNumber().trim();

        validateRoomBelongsToExam(exam, examRoom);
        validateStudentInExamBatch(exam, student);

        boolean studentChanged = !allocation.getStudent().getStudentId().equals(student.getStudentId())
                || !allocation.getExam().getExamId().equals(exam.getExamId());

        if (studentChanged && seatAllocationRepository.existsByExam_ExamIdAndStudent_StudentId(
                exam.getExamId(), student.getStudentId())) {
            throw new IllegalArgumentException("This student already has a seat for the exam");
        }

        boolean seatChanged = !allocation.getExamRoom().getExamRoomId().equals(examRoom.getExamRoomId())
                || !allocation.getSeatNumber().equalsIgnoreCase(seat);

        if (seatChanged && seatAllocationRepository.existsByExamRoom_ExamRoomIdAndSeatNumberIgnoreCase(
                examRoom.getExamRoomId(), seat)) {
            throw new IllegalArgumentException("Seat '" + seat + "' is already taken in this room");
        }

        allocation.setExam(exam);
        allocation.setExamRoom(examRoom);
        allocation.setStudent(student);
        allocation.setSeatNumber(seat);
        allocation.setRowNumber(trimOrNull(request.getRowNumber()));
        allocation.setColumnNumber(trimOrNull(request.getColumnNumber()));

        return mapToDto(seatAllocationRepository.save(allocation));
    }

    // Free up a seat
    @Transactional
    public void delete(UUID allocationId) {
        seatAllocationRepository.delete(findOrThrow(allocationId));
    }

    // The room must be allocated to this exam
    private void validateRoomBelongsToExam(ExamEntity exam, ExamRoomEntity examRoom) {
        if (!examRoom.getExam().getExamId().equals(exam.getExamId())) {
            throw new IllegalArgumentException("That room is not allocated to this exam");
        }
    }

    // Only the exam's own cohort can be seated
    private void validateStudentInExamBatch(ExamEntity exam, StudentEntity student) {
        UUID examBatchId = exam.getBatch() != null ? exam.getBatch().getBatchId() : null;
        UUID studentBatchId = student.getBatch() != null ? student.getBatch().getBatchId() : null;

        if (examBatchId != null && !examBatchId.equals(studentBatchId)) {
            throw new IllegalArgumentException("Student is not in the batch sitting this exam");
        }
    }

    // Stop seating past the allocated capacity
    private void validateRoomHasSpace(ExamRoomEntity examRoom) {
        long seated = seatAllocationRepository.countByExamRoom_ExamRoomId(examRoom.getExamRoomId());
        Integer capacity = examRoom.getAllocatedCapacity();

        if (capacity != null && seated >= capacity) {
            throw new IllegalArgumentException(
                    "Room " + examRoom.getRoom().getRoomCode() + " is full ("
                            + seated + "/" + capacity + " seats used)"
            );
        }
    }

    private SeatAllocationEntity findOrThrow(UUID allocationId) {
        return seatAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Seat allocation not found: " + allocationId));
    }

    private ExamEntity findExamOrThrow(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    }

    private ExamRoomEntity findExamRoomOrThrow(UUID examRoomId) {
        return examRoomRepository.findById(examRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Exam room not found: " + examRoomId));
    }

    private StudentEntity findStudentOrThrow(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
    }

    // Blank strings become null
    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Entity to response DTO
    private SeatAllocationResponseDto mapToDto(SeatAllocationEntity allocation) {
        ExamEntity exam = allocation.getExam();
        ExamRoomEntity examRoom = allocation.getExamRoom();
        StudentEntity student = allocation.getStudent();
        RoomEntity room = examRoom != null ? examRoom.getRoom() : null;

        return SeatAllocationResponseDto.builder()
                .allocationId(allocation.getAllocationId())
                .examId(exam != null ? exam.getExamId() : null)
                .moduleCode(exam != null && exam.getModule() != null ? exam.getModule().getModuleCode() : null)
                .examDate(exam != null ? exam.getExamDate() : null)
                .startTime(exam != null ? exam.getStartTime() : null)
                .endTime(exam != null ? exam.getEndTime() : null)
                .examRoomId(examRoom != null ? examRoom.getExamRoomId() : null)
                .roomId(room != null ? room.getRoomId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .roomName(room != null ? room.getRoomName() : null)
                .studentId(student != null ? student.getStudentId() : null)
                .studentNumber(student != null ? student.getStudentNumber() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getFullName() : null)
                .seatNumber(allocation.getSeatNumber())
                .rowNumber(allocation.getRowNumber())
                .columnNumber(allocation.getColumnNumber())
                .createdAt(allocation.getCreatedAt())
                .updatedAt(allocation.getUpdatedAt())
                .build();
    }
}
