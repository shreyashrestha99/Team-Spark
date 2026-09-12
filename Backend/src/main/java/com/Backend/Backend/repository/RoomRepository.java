package com.Backend.Backend.repository;

import com.Backend.Backend.entity.RoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {

    boolean existsByRoomCodeIgnoreCase(String roomCode);

    // Uniqueness check that skips the edited row
    boolean existsByRoomCodeIgnoreCaseAndRoomIdNot(String roomCode, UUID roomId);

    List<RoomEntity> findAllByIsAvailableTrueOrderByRoomCodeAsc();

    boolean existsByBuilding_BuildingId(UUID buildingId);

    // Combined list, search, building, type and capacity filter.
    // Text filters use "" rather than null so Postgres can type the parameters.
    @Query("SELECT r FROM RoomEntity r WHERE " +
            "(:search = '' OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.roomName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.building.buildingName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:buildingId IS NULL OR r.building.buildingId = :buildingId) AND " +
            "(:roomType = '' OR LOWER(r.roomType) = LOWER(:roomType)) AND " +
            "(:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    Page<RoomEntity> searchRooms(
            @Param("search") String search,
            @Param("buildingId") UUID buildingId,
            @Param("roomType") String roomType,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable
    );

    /** Rooms the generator may consider: available, big enough, and with a usable seat grid. */
    @Query("SELECT r FROM RoomEntity r WHERE r.isAvailable = true AND r.capacity >= :minCapacity " +
            "ORDER BY r.capacity ASC")
    List<RoomEntity> findCandidateRooms(@Param("minCapacity") Integer minCapacity);
}
