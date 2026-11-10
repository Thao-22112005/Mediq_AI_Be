package com.mediqai.appointment.event;

public record AppointmentCreatedEvent(
        Long patientUserId,
        String patientName,
        String appointmentId,
        String appointmentDate,
        String startTime,
        String endTime
) {
}