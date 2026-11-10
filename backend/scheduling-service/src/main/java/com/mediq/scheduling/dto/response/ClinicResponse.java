package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.ClinicStatus;

import java.time.Instant;
import java.util.UUID;

public record ClinicResponse(
        UUID clinicId,
        String code,
        String name,
        String address,
        String phone,
        ClinicStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
