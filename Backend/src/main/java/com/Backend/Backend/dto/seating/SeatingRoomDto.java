package com.Backend.Backend.dto.seating;

import lombok.*;

import java.util.List;
import java.util.UUID;

/** One hall allocated to an exam, with its full desk grid. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingRoomDto {

    private UUID examRoomId;
    private UUID roomId;
    private String roomCode;
    private String roomName;
    private String buildingName;

    private Integer seatRows;
    private Integer seatsPerRow;

    // Teaching seats, and what is left once exam spacing is applied
    private Integer capacity;
    private Integer examCapacity;

    private Integer allocatedCapacity;
    private int seatedHere;

    /** Every desk in the hall, row by row, occupied or not. */
    private List<SeatCellDto> cells;
}
