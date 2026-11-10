package com.mediq.scheduling.event.payload;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestApprovedEventPayload(
        UUID leaveRequestId,
        UUID doctorId,
        LocalDate startDate,
        LocalDate endDate
) {}
