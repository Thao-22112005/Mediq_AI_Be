package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.RoomStatus;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID roomId,
        UUID clinicId,
        UUID specialtyId,
        String code,
        String name,
        String floor,
        String area,
        RoomStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
