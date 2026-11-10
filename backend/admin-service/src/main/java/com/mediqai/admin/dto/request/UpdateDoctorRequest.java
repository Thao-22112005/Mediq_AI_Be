
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
public class UpdateDoctorRequest {

    private String fullName;

    private String phoneNumber;

    private String licenseNumber;

    private String status;

    private List<DoctorSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorSpecialtyRequest {

        private UUID specialtyId;

        private boolean isPrimary;
    }
}
