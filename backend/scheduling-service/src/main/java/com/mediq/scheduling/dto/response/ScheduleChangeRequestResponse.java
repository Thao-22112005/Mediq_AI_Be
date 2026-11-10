package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleChangeRequestResponse(
        UUID requestId,
        UUID scheduleId,
        UUID doctorId,
        LocalTime requestedStartTime,
        LocalTime requestedEndTime,
        UUID requestedRoomId,
        String reason,
        ScheduleChangeRequestStatus status,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
