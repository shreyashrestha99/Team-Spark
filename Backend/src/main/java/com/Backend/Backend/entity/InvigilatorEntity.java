package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invigilators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invigilator_id", updatable = false, nullable = false)
    private UUID invigilatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamEntity exam;

    @Builder.Default
    @Column(name = "role", nullable = false, length = 50)
    private String role = "INVIGILATOR";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
