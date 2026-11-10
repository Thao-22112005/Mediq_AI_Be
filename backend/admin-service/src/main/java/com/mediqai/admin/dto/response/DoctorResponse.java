package com.mediqai.admin.dto.response;

import com.mediqai.admin.dto.response.DoctorSpecialtyResponse;
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
public class DoctorResponse {

    private UUID doctorId;

    private Long userId;

    private String fullName;

    private String phoneNumber;

    private String licenseNumber;

    private String status;

    private List<DoctorSpecialtyResponse> specialties;

    private Instant createdAt;

    private Instant updatedAt;
}