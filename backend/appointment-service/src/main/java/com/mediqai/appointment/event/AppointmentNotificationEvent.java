
package com.mediqai.appointment.event;

public record AppointmentNotificationEvent(
        Long patientId,
        String appointmentId,
        String eventType,
        String status,
        String appointmentDate,
        String startTime,
        String endTime,
        String oldAppointmentDate,
        String oldStartTime,
        String oldEndTime
) {
}
