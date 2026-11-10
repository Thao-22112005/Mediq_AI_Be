package com.mediqai.appointment.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleResponse {

    private UUID scheduleId;

    private UUID doctorId;

    private UUID clinicId;

    private UUID specialtyId;

    private UUID roomId;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}