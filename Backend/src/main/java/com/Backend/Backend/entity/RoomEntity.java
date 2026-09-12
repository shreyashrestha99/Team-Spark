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
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_id", updatable = false, nullable = false)
    private UUID roomId;

    @Column(name = "room_code", nullable = false, unique = true, length = 50)
    private String roomCode;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "floor")
    private Integer floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private BuildingEntity building;

    // Seat grid. Rows are derived from capacity and seats per row when the room is saved.
    @Column(name = "seat_rows")
    private Integer seatRows;

    @Column(name = "seats_per_row")
    private Integer seatsPerRow;

    /** Share of the seat grid usable under exam spacing, e.g. 0.5 keeps every other column free. */
    @Builder.Default
    @Column(name = "exam_capacity_factor", nullable = false)
    private Double examCapacityFactor = 0.5;

    @Builder.Default
    @Column(name = "has_projector", nullable = false)
    private Boolean hasProjector = false;

    @Builder.Default
    @Column(name = "has_computers", nullable = false)
    private Boolean hasComputers = false;

    @Builder.Default
    @Column(name = "has_ac", nullable = false)
    private Boolean hasAc = false;

    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "room")
    private List<TimetableSessionEntity> timetableSessions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "room")
    private List<ExamRoomEntity> examRooms = new ArrayList<>();
}
