package com.mediqai.appointment.dto.response;

import com.mediqai.appointment.entity.AppointmentStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentHistoryResponse {

    private UUID historyId;

    private UUID appointmentId;

    private AppointmentStatus oldStatus;

    private AppointmentStatus newStatus;

    private UUID oldSlotId;
    private UUID newSlotId;
    private LocalTime oldStartTime;
    private LocalTime newStartTime;

    private UUID changedBy;

    private String reason;

    private Instant createdAt;
}