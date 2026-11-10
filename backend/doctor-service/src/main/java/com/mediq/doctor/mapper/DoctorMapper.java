package com.mediq.doctor.mapper;

import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.Doctor;
import com.mediq.doctor.entity.DoctorSpecialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "isPrimary", source = "primary")
    DoctorSpecialtyResponse toSpecialtyResponse(DoctorSpecialty specialty);

    List<DoctorSpecialtyResponse> toSpecialtyResponseList(List<DoctorSpecialty> specialties);

    DoctorResponse toDoctorResponse(Doctor doctor);

    List<DoctorResponse> toDoctorResponseList(List<Doctor> doctors);
}
