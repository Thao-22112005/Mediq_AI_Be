package com.mediqai.appointment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleAppointmentRequest {

    @NotNull
    private UUID newSlotId;

    private String reason;
}