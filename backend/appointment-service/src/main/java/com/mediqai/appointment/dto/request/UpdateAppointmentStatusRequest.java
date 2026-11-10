package com.mediqai.appointment.dto.request;

import com.mediqai.appointment.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppointmentStatusRequest {

    @NotNull
    private AppointmentStatus status;

    private String reason;
}