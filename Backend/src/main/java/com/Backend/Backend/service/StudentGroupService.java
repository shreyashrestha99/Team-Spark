package com.Backend.Backend.service;

import com.Backend.Backend.dto.PageResponseDto;
import com.Backend.Backend.dto.studentgroup.StudentGroupRequestDto;
import com.Backend.Backend.dto.studentgroup.StudentGroupResponseDto;
import com.Backend.Backend.entity.BatchEntity;
import com.Backend.Backend.entity.StudentEntity;
import com.Backend.Backend.entity.StudentGroupEntity;
import com.Backend.Backend.repository.BatchRepository;
import com.Backend.Backend.repository.StudentGroupRepository;
import com.Backend.Backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;
    private final BatchRepository batchRepository;
    private final StudentRepository studentRepository;

    // Create a tutorial or workshop group inside a batch
    @Transactional
    public StudentGroupResponseDto create(StudentGroupRequestDto request) {
        BatchEntity batch = findBatchOrThrow(request.getBatchId());
        String name = request.getGroupName().trim();

        if (studentGroupRepository.existsByBatch_BatchIdAndGroupNameIgnoreCase(batch.getBatchId(), name)) {
            throw new IllegalArgumentException("Group '" + name + "' already exists in this batch");
        }

        StudentGroupEntity group = StudentGroupEntity.builder()
                .batch(batch)
                .groupName(name)
                .build();

        return mapToDto(studentGroupRepository.save(group));
    }

    // Paged list with an optional batch filter
    @Transactional(readOnly = true)
    public PageResponseDto<StudentGroupResponseDto> getAll(
            UUID batchId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentGroupEntity> result = studentGroupRepository.searchGroups(batchId, pageable);
        return PageResponseDto.from(result.map(this::mapToDto));
    }

    // Groups of one batch for dropdowns
    @Transactional(readOnly = true)
    public List<StudentGroupResponseDto> getByBatch(UUID batchId) {
        return studentGroupRepository.findAllByBatch_BatchIdOrderByGroupNameAsc(batchId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Fetch one group by id
    @Transactional(readOnly = true)
    public StudentGroupResponseDto getById(UUID groupId) {
        return mapToDto(findOrThrow(groupId));
    }

    /**
     * Splits a batch into the requested number of groups and spreads its students evenly.
     * Saves the admin from creating groups and assigning every student one at a time.
     */
    @Transactional
    public List<StudentGroupResponseDto> autoSplit(UUID batchId, int groupCount) {
        if (groupCount < 1 || groupCount > 26) {
            throw new IllegalArgumentException("Group count must be between 1 and 26");
        }

        BatchEntity batch = findBatchOrThrow(batchId);
        List<StudentGroupEntity> existing =
                studentGroupRepository.findAllByBatch_BatchIdOrderByGroupNameAsc(batchId);

        if (!existing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Batch already has " + existing.size() + " group(s). Remove them before splitting again."
            );
        }

        List<StudentGroupEntity> groups = new ArrayList<>();
        for (int index = 0; index < groupCount; index++) {
            groups.add(studentGroupRepository.save(StudentGroupEntity.builder()
                    .batch(batch)
                    .groupName("Group " + (char) ('A' + index))
                    .build()));
        }

        // Round-robin keeps the groups within one student of each other
        List<StudentEntity> students = studentRepository.findAllByBatch_BatchId(batchId);
        for (int index = 0; index < students.size(); index++) {
            StudentEntity student = students.get(index);
            student.setStudentGroup(groups.get(index % groupCount));
            studentRepository.save(student);
        }

        return groups.stream().map(this::mapToDto).toList();
    }

    // Rename a group
    @Transactional
    public StudentGroupResponseDto update(UUID groupId, StudentGroupRequestDto request) {
        StudentGroupEntity group = findOrThrow(groupId);
        BatchEntity batch = findBatchOrThrow(request.getBatchId());
        String name = request.getGroupName().trim();

        if (studentGroupRepository.existsByBatch_BatchIdAndGroupNameIgnoreCaseAndGroupIdNot(
                batch.getBatchId(), name, groupId)) {
            throw new IllegalArgumentException("Group '" + name + "' already exists in this batch");
        }

        group.setBatch(batch);
        group.setGroupName(name);

        return mapToDto(studentGroupRepository.save(group));
    }

    // Delete only once no student sits in it
    @Transactional
    public void delete(UUID groupId) {
        StudentGroupEntity group = findOrThrow(groupId);
        int studentCount = group.getStudents() != null ? group.getStudents().size() : 0;

        if (studentCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete: group still has " + studentCount + " student(s). Move them first."
            );
        }

        studentGroupRepository.delete(group);
    }

    // Shared lookup with a clear error
    private StudentGroupEntity findOrThrow(UUID groupId) {
        return studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    private BatchEntity findBatchOrThrow(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    // Entity to response DTO
    private StudentGroupResponseDto mapToDto(StudentGroupEntity group) {
        BatchEntity batch = group.getBatch();

        return StudentGroupResponseDto.builder()
                .groupId(group.getGroupId())
                .batchId(batch != null ? batch.getBatchId() : null)
                .batchName(batch != null ? batch.getBatchName() : null)
                .groupName(group.getGroupName())
                .studentCount(group.getStudents() != null ? group.getStudents().size() : 0)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
