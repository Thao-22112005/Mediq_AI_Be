package com.mediq.doctor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddDoctorSpecialtyRequest {

    private UUID specialtyId;

    private boolean isPrimary;

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
