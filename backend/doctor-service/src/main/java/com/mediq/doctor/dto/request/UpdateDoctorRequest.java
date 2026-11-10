package com.mediq.doctor.dto.request;

import com.mediq.doctor.entity.DoctorStatus;
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
public class UpdateDoctorRequest {

    private String fullName;

    private String phoneNumber;

    private String licenseNumber;

    private DoctorStatus status;

    private List<DoctorSpecialtyRequest> specialties;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorSpecialtyRequest {

        private UUID specialtyId;

        private boolean isPrimary;

        public void setIsPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }
}