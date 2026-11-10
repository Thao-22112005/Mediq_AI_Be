package com.mediq.scheduling.event.payload;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleApprovedEventPayload(
        UUID scheduleId,
        UUID doctorId,
        UUID clinicId,
        UUID roomId,
        UUID specialtyId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
