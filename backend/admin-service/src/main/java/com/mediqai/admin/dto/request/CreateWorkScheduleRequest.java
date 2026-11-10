package com.mediqai.admin.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateWorkScheduleRequest(
        UUID doctorId,
        UUID clinicId,
        UUID specialtyId,
        UUID roomId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
}