package com.Backend.Backend.scheduling;

import com.Backend.Backend.entity.RoomEntity;
import lombok.*;

/** A requirement that has been given a room and a starting slot in the weekly grid. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedSession {

    private SessionRequirement requirement;

    // Index into the context's teaching slot list
    private int startSlotIndex;

    // How many consecutive slots the session covers
    private int slotSpan;

    private RoomEntity room;

    public int endSlotIndexExclusive() {
        return startSlotIndex + slotSpan;
    }
}
