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
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "module_id", updatable = false, nullable = false)
    private UUID moduleId;

    @Column(name = "module_code", nullable = false, unique = true, length = 50)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 150)
    private String moduleName;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @Column(name = "semester", nullable = false)
    private Integer semester;

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
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchModuleEntity> batchModules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "module")
    private List<TimetableSessionEntity> timetableSessions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "module")
    private List<ExamEntity> exams = new ArrayList<>();
}
