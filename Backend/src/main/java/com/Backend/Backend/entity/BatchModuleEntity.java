package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_modules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"batch_id", "module_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_module_id", updatable = false, nullable = false)
    private UUID batchModuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchEntity batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    // Lecturer who delivers this module to this batch. Left null the generator picks the lightest loaded.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacher;

    // Delivery pattern: how one week of this module is taught.
    // The generator expands these counts into the session requirements it must place.
    @Builder.Default
    @Column(name = "lecture_sessions_per_week", nullable = false)
    private Integer lectureSessionsPerWeek = 1;

    @Builder.Default
    @Column(name = "lecture_duration_minutes", nullable = false)
    private Integer lectureDurationMinutes = 120;

    @Builder.Default
    @Column(name = "tutorial_sessions_per_week", nullable = false)
    private Integer tutorialSessionsPerWeek = 1;

    @Builder.Default
    @Column(name = "tutorial_duration_minutes", nullable = false)
    private Integer tutorialDurationMinutes = 60;

    @Builder.Default
    @Column(name = "workshop_sessions_per_week", nullable = false)
    private Integer workshopSessionsPerWeek = 1;

    @Builder.Default
    @Column(name = "workshop_duration_minutes", nullable = false)
    private Integer workshopDurationMinutes = 120;

    // Lectures take the whole cohort, tutorials and workshops usually run per group
    @Builder.Default
    @Column(name = "split_tutorial_by_group", nullable = false)
    private Boolean splitTutorialByGroup = true;

    @Builder.Default
    @Column(name = "split_workshop_by_group", nullable = false)
    private Boolean splitWorkshopByGroup = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
