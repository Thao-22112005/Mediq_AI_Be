package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorScheduleResponse {

    private UUID scheduleId;

    private UUID clinicId;

    private UUID specialtyId;

    private UUID roomId;

    private String roomCode;

    private String roomName;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;
}