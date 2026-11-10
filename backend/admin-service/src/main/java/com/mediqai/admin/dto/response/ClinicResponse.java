 package com.mediqai.admin.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClinicResponse(
        UUID clinicId,
        String code,
        String name,
        String address,
        String phone,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
