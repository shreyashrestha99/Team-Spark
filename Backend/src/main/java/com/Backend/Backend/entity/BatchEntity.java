package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", updatable = false, nullable = false)
    private UUID batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false)
    private ProgrammeEntity programme;

    @Column(name = "batch_name", nullable = false, length = 100)
    private String batchName;

    // Year the cohort started, e.g. 2025
    @Column(name = "intake_year")
    private Integer intakeYear;

    // MORNING or DAY, restricts the generator to that half of the grid
    @Column(name = "shift", length = 30)
    private String shift;

    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
    private List<StudentEntity> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentGroupEntity> studentGroups = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchModuleEntity> batchModules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "batch")
    private List<TimetableSessionEntity> timetableSessions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "batch")
    private List<ExamEntity> exams = new ArrayList<>();
}
