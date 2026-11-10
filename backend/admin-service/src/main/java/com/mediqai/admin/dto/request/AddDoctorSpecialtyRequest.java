package com.mediqai.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDoctorSpecialtyRequest {

    private UUID specialtyId;

    private boolean isPrimary;
}