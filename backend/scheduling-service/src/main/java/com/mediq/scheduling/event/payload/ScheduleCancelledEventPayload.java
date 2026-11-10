package com.mediq.scheduling.event.payload;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleCancelledEventPayload(
        UUID scheduleId,
        UUID doctorId,
        UUID clinicId,
        LocalDate date
) {}
