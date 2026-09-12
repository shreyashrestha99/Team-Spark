package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "programmes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "programme_id", updatable = false, nullable = false)
    private UUID programmeId;

    @Column(name = "programme_name", nullable = false, length = 150)
    private String programmeName;

    @Column(name = "programme_code", nullable = false, unique = true, length = 50)
    private String programmeCode;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

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
    @OneToMany(mappedBy = "programme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchEntity> batches = new ArrayList<>();
}
