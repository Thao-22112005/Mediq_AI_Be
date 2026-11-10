package com.mediq.scheduling.dto.request;

import com.mediq.scheduling.entity.ClinicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClinicRequest {

    private String name;
    private String address;
    private String phone;
    private ClinicStatus status;
}
