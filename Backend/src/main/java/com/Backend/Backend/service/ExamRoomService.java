package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.examroom.ExamRoomRequestDto;
import com.Backend.Backend.dto.examroom.ExamRoomResponseDto;
import com.Backend.Backend.entity.ExamEntity;
import com.Backend.Backend.entity.ExamRoomEntity;
import com.Backend.Backend.entity.RoomEntity;
import com.Backend.Backend.repository.ExamRepository;
import com.Backend.Backend.repository.ExamRoomRepository;
import com.Backend.Backend.repository.RoomRepository;
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
public class ExamRoomService {

    private final ExamRoomRepository examRoomRepository;
    private final ExamRepository examRepository;
    private final RoomRepository roomRepository;

    // Allocate a room to an exam
    @Transactional
    public ExamRoomResponseDto create(ExamRoomRequestDto request) {
        if (examRoomRepository.existsByExam_ExamIdAndRoom_RoomId(request.getExamId(), request.getRoomId())) {
            throw new IllegalArgumentException("This room is already allocated to the exam");
        }

        ExamEntity exam = findExamOrThrow(request.getExamId());
        RoomEntity room = findRoomOrThrow(request.getRoomId());

        validateCapacity(room, request.getAllocatedCapacity());
        rejectIfRoomClashes(exam, room);

        ExamRoomEntity examRoom = ExamRoomEntity.builder()
                .exam(exam)
                .room(room)
                .allocatedCapacity(request.getAllocatedCapacity())
                .seatCount(request.getSeatCount())
                .status(hasText(request.getStatus()) ? request.getStatus().trim() : "ALLOCATED")
                .build();

        return mapToDto(examRoomRepository.save(examRoom));
    }

    // Paged list with exam and room filters
    @Transactional(readOnly = true)
    public PageResponseDto<ExamRoomResponseDto> getAll(
            UUID examId,
            UUID roomId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExamRoomEntity> result = examRoomRepository.searchExamRooms(examId, roomId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // All rooms for one exam
    @Transactional(readOnly = true)
    public List<ExamRoomResponseDto> getByExam(UUID examId) {
        return examRoomRepository.findAllByExam_ExamId(examId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one allocation by id
    @Transactional(readOnly = true)
    public ExamRoomResponseDto getById(UUID examRoomId) {
        return mapToDto(findOrThrow(examRoomId));
    }

    // Update an allocation
    @Transactional
    public ExamRoomResponseDto update(UUID examRoomId, ExamRoomRequestDto request) {
        ExamRoomEntity examRoom = findOrThrow(examRoomId);

        boolean roomChanged = !examRoom.getRoom().getRoomId().equals(request.getRoomId())
                || !examRoom.getExam().getExamId().equals(request.getExamId());

        if (roomChanged && examRoomRepository.existsByExam_ExamIdAndRoom_RoomId(
                request.getExamId(), request.getRoomId())) {
            throw new IllegalArgumentException("This room is already allocated to the exam");
        }

        ExamEntity exam = findExamOrThrow(request.getExamId());
        RoomEntity room = findRoomOrThrow(request.getRoomId());

        validateCapacity(room, request.getAllocatedCapacity());
        if (roomChanged) {
            rejectIfRoomClashes(exam, room);
        }

        // Seats already placed must still fit
        int seatsPlaced = examRoom.getSeatAllocations() != null ? examRoom.getSeatAllocations().size() : 0;
        if (request.getAllocatedCapacity() < seatsPlaced) {
            throw new IllegalArgumentException(
                    "Cannot reduce capacity to " + request.getAllocatedCapacity()
                            + ": " + seatsPlaced + " seat(s) already allocated"
            );
        }

        examRoom.setExam(exam);
        examRoom.setRoom(room);
        examRoom.setAllocatedCapacity(request.getAllocatedCapacity());
        examRoom.setSeatCount(request.getSeatCount());
        if (hasText(request.getStatus())) {
            examRoom.setStatus(request.getStatus().trim());
        }

        return mapToDto(examRoomRepository.save(examRoom));
    }

    // Release a room from an exam
    @Transactional
    public void delete(UUID examRoomId) {
        ExamRoomEntity examRoom = findOrThrow(examRoomId);

        int seats = examRoom.getSeatAllocations() != null ? examRoom.getSeatAllocations().size() : 0;
        if (seats > 0) {
            throw new IllegalArgumentException(
                    "Cannot remove: " + seats + " seat(s) are allocated in this room."
            );
        }

        examRoomRepository.delete(examRoom);
    }

    // Allocation cannot exceed physical capacity
    private void validateCapacity(RoomEntity room, Integer allocatedCapacity) {
        if (room.getCapacity() != null && allocatedCapacity > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Allocated capacity " + allocatedCapacity + " exceeds room capacity "
                            + room.getCapacity() + " for " + room.getRoomCode()
            );
        }
    }

    // One hall cannot host two overlapping exams
    private void rejectIfRoomClashes(ExamEntity exam, RoomEntity room) {
        List<ExamRoomEntity> clashes = examRoomRepository.findRoomClashes(
                room.getRoomId(),
                exam.getExamId(),
                exam.getExamDate(),
                exam.getStartTime(),
                exam.getEndTime()
        );

        if (!clashes.isEmpty()) {
            ExamEntity other = clashes.get(0).getExam();
            throw new IllegalArgumentException(
                    "Room " + room.getRoomCode() + " is already used by exam "
                            + other.getModule().getModuleCode() + " on " + other.getExamDate()
                            + " at " + other.getStartTime() + "–" + other.getEndTime()
            );
        }
    }

    private ExamRoomEntity findOrThrow(UUID examRoomId) {
        return examRoomRepository.findById(examRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Exam room not found: " + examRoomId));
    }

    private ExamEntity findExamOrThrow(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    }

    private RoomEntity findRoomOrThrow(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Entity to response DTO
    private ExamRoomResponseDto mapToDto(ExamRoomEntity examRoom) {
        ExamEntity exam = examRoom.getExam();
        RoomEntity room = examRoom.getRoom();

        return ExamRoomResponseDto.builder()
                .examRoomId(examRoom.getExamRoomId())
                .examId(exam != null ? exam.getExamId() : null)
                .moduleCode(exam != null && exam.getModule() != null ? exam.getModule().getModuleCode() : null)
                .batchName(exam != null && exam.getBatch() != null ? exam.getBatch().getBatchName() : null)
                .examDate(exam != null ? exam.getExamDate() : null)
                .startTime(exam != null ? exam.getStartTime() : null)
                .endTime(exam != null ? exam.getEndTime() : null)
                .roomId(room != null ? room.getRoomId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .roomName(room != null ? room.getRoomName() : null)
                .roomCapacity(room != null ? room.getCapacity() : null)
                .allocatedCapacity(examRoom.getAllocatedCapacity())
                .seatCount(examRoom.getSeatCount())
                .status(examRoom.getStatus())
                .seatsAllocated(examRoom.getSeatAllocations() != null ? examRoom.getSeatAllocations().size() : 0)
                .createdAt(examRoom.getCreatedAt())
                .updatedAt(examRoom.getUpdatedAt())
                .build();
    }
}
