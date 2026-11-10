package com.mediqai.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AdminAppointmentResponse(
        UUID appointmentId,
        Long patientId,
        UUID doctorId,
        UUID clinicId,
        UUID specialtyId,
        UUID roomId,
        UUID slotId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Thông tin hiển thị
        String patientNames,
        String clinicNames,
        String doctorNames,
        String specialtyNames,
        String roomNames
) {}