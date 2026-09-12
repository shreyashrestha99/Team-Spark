package com.Backend.Backend.dto.seating;

import lombok.*;

import java.util.UUID;

/**
 * One physical desk in a hall. Empty cells are returned too, because the gaps are
 * the spacing that stops candidates reading each other's papers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatCellDto {

    /** Label the invigilator reads off the desk, e.g. "C4". */
    private String seatNumber;

    // Row letter and 1-based column, the structural truth behind the label
    private String rowLabel;
    private Integer columnNumber;

    private boolean occupied;

    private UUID studentId;
    private String studentName;
    private String studentNumber;

    /** Module the occupant is sitting. Differs from the room's own exam in a shared hall. */
    private String moduleCode;

    // True when another exam sharing this hall holds the seat
    private boolean otherExam;
}
