package com.mediqai.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorRequest {

    private Long userId;

    private String fullName;

    private String phoneNumber;

    private String licenseNumber;

    private List<InitialSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialSpecialtyRequest {

        private UUID specialtyId;

        private boolean isPrimary;
    }
}