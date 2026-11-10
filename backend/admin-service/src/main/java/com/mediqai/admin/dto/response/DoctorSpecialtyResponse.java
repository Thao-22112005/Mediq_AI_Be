package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialtyResponse {

    private UUID doctorSpecialtyId;

    private UUID doctorId;

    private UUID specialtyId;

    private boolean isPrimary;

    private Instant createdAt;
}