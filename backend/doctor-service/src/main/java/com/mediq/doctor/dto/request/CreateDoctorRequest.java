package com.mediq.doctor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorRequest {

    private Long userId;

    private String fullName;

    private String phoneNumber;

   
    private String licenseNumber;

    private List<InitialSpecialtyRequest> specialties;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitialSpecialtyRequest {
        private UUID specialtyId;
        private boolean isPrimary;

        public void setIsPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }
}
