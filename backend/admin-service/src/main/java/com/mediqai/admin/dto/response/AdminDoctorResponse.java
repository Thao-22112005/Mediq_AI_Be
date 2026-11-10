package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorResponse {

    private UUID doctorId;

    private Long userId;

    private String fullName;

    private String phoneNumber;

    private String licenseNumber;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    private List<AdminDoctorSpecialtyResponse> specialties;

    private List<AdminDoctorScheduleResponse> workSchedules;
}