package com.mediq.scheduling.client;

import java.util.Optional;
import java.util.UUID;

public interface DoctorClient {

    record DoctorDto(
            UUID doctorId,
            String fullName,
            String status
    ) {}

    boolean hasSpecialty(UUID doctorId, UUID specialtyId);

    Optional<DoctorDto> getDoctor(UUID doctorId);

    boolean isDoctorActive(UUID doctorId);
}
