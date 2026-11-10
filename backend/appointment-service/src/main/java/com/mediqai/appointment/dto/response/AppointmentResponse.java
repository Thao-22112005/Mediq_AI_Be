
package com.mediqai.appointment.dto.response;

import com.mediqai.appointment.entity.AppointmentStatus;
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
public class AppointmentResponse {

    private UUID appointmentId;
    private Long patientId;
    private String patientName;

    private UUID doctorId;
    private String doctorName;

    private UUID clinicId;
    private String clinicName;

    private UUID specialtyId;
    private String specialtyName;

    private UUID roomId;
    private String roomName;
    private String roomCode;

    private UUID slotId;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private AppointmentStatus status;
    private String reason;

    private Instant createdAt;
    private Instant updatedAt;
}