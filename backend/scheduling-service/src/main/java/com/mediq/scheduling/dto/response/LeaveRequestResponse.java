package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.LeaveRequestStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveRequestResponse(
        UUID leaveRequestId,
        UUID doctorId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String reason,
        LeaveRequestStatus status,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {}
