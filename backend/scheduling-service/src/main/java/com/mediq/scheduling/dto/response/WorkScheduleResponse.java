package com.mediq.scheduling.dto.response;

import com.mediq.scheduling.entity.WorkScheduleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


public record WorkScheduleResponse(
        UUID scheduleId,
        UUID doctorId,
        String doctorName,
        UUID clinicId,
        String clinicName,
        UUID specialtyId,
        String specialtyName,
        UUID roomId,
        String roomName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        WorkScheduleStatus status,
        Instant createdAt,
        Instant updatedAt
) {}

