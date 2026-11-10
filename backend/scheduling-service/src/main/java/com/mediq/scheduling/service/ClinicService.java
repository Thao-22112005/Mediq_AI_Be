package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateClinicRequest;
import com.mediq.scheduling.dto.request.UpdateClinicRequest;
import com.mediq.scheduling.dto.response.ClinicResponse;
import com.mediq.scheduling.entity.ClinicStatus;

import java.util.List;
import java.util.UUID;

public interface ClinicService {

    ClinicResponse createClinic(CreateClinicRequest request);

    ClinicResponse getClinicById(UUID clinicId);

    List<ClinicResponse> getAllClinics(ClinicStatus status);

    ClinicResponse updateClinic(UUID clinicId, UpdateClinicRequest request);

    ClinicResponse activateClinic(UUID clinicId);

    ClinicResponse deactivateClinic(UUID clinicId);
}
