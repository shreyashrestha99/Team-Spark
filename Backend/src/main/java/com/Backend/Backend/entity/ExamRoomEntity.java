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
@Table(name = "exam_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "exam_room_id", updatable = false, nullable = false)
    private UUID examRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamEntity exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(name = "allocated_capacity", nullable = false)
    private Integer allocatedCapacity;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "ALLOCATED";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "examRoom", cascade = CascadeType.ALL)
    private List<SeatAllocationEntity> seatAllocations = new ArrayList<>();
}
