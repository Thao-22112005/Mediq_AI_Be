package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.SlotStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SlotResponse(
        UUID slotId,
        UUID scheduleId,
        UUID doctorId,
        UUID clinicId,
        UUID specialtyId,
        UUID roomId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        SlotStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
