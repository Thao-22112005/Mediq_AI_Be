package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.SpecialtyStatus;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyResponse(
        UUID specialtyId,
        String name,
        String description,
        SpecialtyStatus status,
        Integer defaultSlotDuration,
        Instant createdAt,
        Instant updatedAt
) {
}
