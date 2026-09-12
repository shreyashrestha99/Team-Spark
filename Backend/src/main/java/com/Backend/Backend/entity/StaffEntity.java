package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "staff_id", updatable = false, nullable = false)
    private UUID staffId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", length = 100)
    private String designation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
