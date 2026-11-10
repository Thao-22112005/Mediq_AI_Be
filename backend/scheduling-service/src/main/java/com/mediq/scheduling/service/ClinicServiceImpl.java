package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateClinicRequest;
import com.mediq.scheduling.dto.request.UpdateClinicRequest;
import com.mediq.scheduling.dto.response.ClinicResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.ClinicMapper;
import com.mediq.scheduling.repository.ClinicRepository;
import com.mediq.scheduling.validation.ClinicValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicMapper clinicMapper;
    private final ClinicValidator clinicValidator;

    @Override
    @Transactional
    public ClinicResponse createClinic(CreateClinicRequest request) {
        clinicValidator.validateCreate(request);

        String trimmedCode = request.getCode().trim();
        if (clinicRepository.existsByCode(trimmedCode)) {
            throw new BusinessException(ErrorCode.CLINIC_CODE_ALREADY_EXISTS,
                    "Clinic with code '" + trimmedCode + "' already exists");
        }

        Clinic clinic = Clinic.builder()
                .code(trimmedCode)
                .name(request.getName().trim())
                .address(request.getAddress())
                .phone(request.getPhone())
                .status(ClinicStatus.ACTIVE)
                .rooms(new ArrayList<>())
                .build();

        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponse(savedClinic);
    }

    @Override
    public ClinicResponse getClinicById(UUID clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + clinicId));
        return clinicMapper.toClinicResponse(clinic);
    }

    @Override
    public List<ClinicResponse> getAllClinics(ClinicStatus status) {
        List<Clinic> clinics = (status != null)
                ? clinicRepository.findByStatus(status)
                : clinicRepository.findAll();
        return clinicMapper.toClinicResponseList(clinics);
    }

    @Override
    @Transactional
    public ClinicResponse updateClinic(UUID clinicId, UpdateClinicRequest request) {
        clinicValidator.validateUpdate(request);

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + clinicId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            clinic.setName(request.getName().trim());
        }

        if (request.getAddress() != null) {
            clinic.setAddress(request.getAddress());
        }

        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }

        if (request.getStatus() != null) {
            clinic.setStatus(request.getStatus());
        }

        Clinic updatedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponse(updatedClinic);
    }

    @Override
    @Transactional
    public ClinicResponse activateClinic(UUID clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + clinicId));
        clinic.setStatus(ClinicStatus.ACTIVE);
        Clinic updatedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponse(updatedClinic);
    }

    @Override
    @Transactional
    public ClinicResponse deactivateClinic(UUID clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + clinicId));
        clinic.setStatus(ClinicStatus.INACTIVE);
        Clinic updatedClinic = clinicRepository.save(clinic);
        return clinicMapper.toClinicResponse(updatedClinic);
    }
}
