package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "exam_id", updatable = false, nullable = false)
    private UUID examId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchEntity batch;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "exam_type", length = 50)
    private String examType;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "SCHEDULED";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamRoomEntity> examRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvigilatorEntity> invigilators = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatAllocationEntity> seatAllocations = new ArrayList<>();
}
