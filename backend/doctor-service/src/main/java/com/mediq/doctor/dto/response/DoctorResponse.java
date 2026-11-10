package com.mediq.doctor.dto.response;

import com.mediq.doctor.entity.DoctorStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DoctorResponse(
        UUID doctorId,
        Long userId,
        String fullName,
        String phoneNumber,
        String licenseNumber,
        DoctorStatus status,
        List<DoctorSpecialtyResponse> specialties,
        Instant createdAt,
        Instant updatedAt
) {}
