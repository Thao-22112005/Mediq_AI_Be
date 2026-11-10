package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.SpecialtyResponse;
import com.mediq.scheduling.entity.Specialty;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    SpecialtyResponse toSpecialtyResponse(Specialty specialty);

    List<SpecialtyResponse> toSpecialtyResponseList(List<Specialty> specialties);
}
