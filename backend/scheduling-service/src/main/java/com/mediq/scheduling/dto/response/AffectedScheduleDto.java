package com.mediq.scheduling.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record AffectedScheduleDto(
        UUID scheduleId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        UUID clinicId,
        UUID roomId,
        UUID specialtyId,
        String handlingType,
        List<UUID> bookedSlotIds
) {}
