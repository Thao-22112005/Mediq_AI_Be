package com.mediq.scheduling.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReplacementCandidateDto(
        UUID slotId,
        UUID doctorId,
        String doctorName,
        UUID clinicId,
        String clinicName,
        UUID roomId,
        String roomName,
        UUID specialtyId,
        String specialtyName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean isCrossClinic,
        int priority
) {}
