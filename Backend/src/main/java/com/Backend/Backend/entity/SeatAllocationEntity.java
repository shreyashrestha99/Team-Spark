package com.Backend.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seat_allocations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "student_id"}),
        @UniqueConstraint(columnNames = {"exam_room_id", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "allocation_id", updatable = false, nullable = false)
    private UUID allocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamEntity exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_room_id", nullable = false)
    private ExamRoomEntity examRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "seat_number", nullable = false, length = 30)
    private String seatNumber;

    @Column(name = "row_number", length = 20)
    private String rowNumber;

    @Column(name = "column_number", length = 20)
    private String columnNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
