package com.mediq.scheduling.dto.request;

import com.mediq.scheduling.entity.SpecialtyStatus;
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
public class UpdateSpecialtyRequest {

    private String name;
    private String description;
    private Integer defaultSlotDuration;
    private SpecialtyStatus status;
}
