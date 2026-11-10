package com.mediq.doctor.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DoctorSpecialtyResponse(
        UUID doctorSpecialtyId,
        UUID doctorId,
        UUID specialtyId,
        boolean isPrimary,
        Instant createdAt
) {}
