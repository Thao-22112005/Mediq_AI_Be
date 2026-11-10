package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateSpecialtyRequest;
import com.mediq.scheduling.dto.request.UpdateSpecialtyRequest;
import com.mediq.scheduling.dto.response.SpecialtyResponse;
import com.mediq.scheduling.entity.SpecialtyStatus;

import java.util.List;
import java.util.UUID;

public interface SpecialtyService {

    SpecialtyResponse createSpecialty(CreateSpecialtyRequest request);

    SpecialtyResponse getSpecialtyById(UUID specialtyId);

    List<SpecialtyResponse> getAllSpecialties(SpecialtyStatus status);

    SpecialtyResponse updateSpecialty(UUID specialtyId, UpdateSpecialtyRequest request);
}
