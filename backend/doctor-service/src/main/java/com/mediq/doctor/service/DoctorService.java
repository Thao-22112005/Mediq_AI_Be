package com.mediq.doctor.service;

import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.request.UpdateDoctorRequest;
import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.DoctorStatus;

import java.util.List;
import java.util.UUID;

public interface DoctorService {

    DoctorResponse createDoctor(CreateDoctorRequest request);

    DoctorResponse getDoctorById(UUID doctorId);

    List<DoctorResponse> getAllDoctors(DoctorStatus status);

    DoctorResponse updateDoctor(UUID doctorId, UpdateDoctorRequest request);

    DoctorSpecialtyResponse addSpecialty(UUID doctorId, AddDoctorSpecialtyRequest request);

    void removeSpecialty(UUID doctorId, UUID specialtyId);

    List<DoctorSpecialtyResponse> getDoctorSpecialties(UUID doctorId);
}
