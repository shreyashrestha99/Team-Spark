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

/**
 * One run of the routine generator. Every session it produced points back here,
 * so a whole generated routine can be reviewed, regenerated or rolled back as a unit.
 */
@Entity
@Table(name = "generation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "run_id", updatable = false, nullable = false)
    private UUID runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchEntity batch;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    // COMPLETED when every requirement was placed, PARTIAL when some could not be
    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "COMPLETED";

    @Column(name = "requirements_total", nullable = false)
    private Integer requirementsTotal;

    @Column(name = "requirements_placed", nullable = false)
    private Integer requirementsPlaced;

    // Soft-constraint penalty of the weekly pattern, lower is better
    @Column(name = "penalty_score", nullable = false)
    private Integer penaltyScore;

    @Column(name = "sessions_created", nullable = false)
    private Integer sessionsCreated;

    // Why anything unplaced could not be placed
    @Column(name = "notes", length = 4000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "generationRun")
    private List<TimetableSessionEntity> sessions = new ArrayList<>();
}
