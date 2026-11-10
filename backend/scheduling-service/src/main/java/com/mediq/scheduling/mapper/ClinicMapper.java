package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.ClinicResponse;
import com.mediq.scheduling.entity.Clinic;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClinicMapper {

    ClinicResponse toClinicResponse(Clinic clinic);

    List<ClinicResponse> toClinicResponseList(List<Clinic> clinics);
}
