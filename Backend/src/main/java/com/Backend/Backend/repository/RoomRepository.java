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

    // Combined list, search, type and capacity filter
    @Query("SELECT r FROM RoomEntity r WHERE " +
            "(:search IS NULL OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.roomName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.building) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:roomType IS NULL OR LOWER(r.roomType) = LOWER(:roomType)) AND " +
            "(:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    Page<RoomEntity> searchRooms(
            @Param("search") String search,
            @Param("roomType") String roomType,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable
    );
}
