package com.mediqai.appointment.dto.response;

import java.util.UUID;

public record SpecialtyAppointmentStatsResponse(
        UUID specialtyId,
        Long patientsThisMonth
) {
}