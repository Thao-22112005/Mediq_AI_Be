package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateSpecialtyRequest;
import com.mediq.scheduling.dto.request.UpdateSpecialtyRequest;
import com.mediq.scheduling.dto.response.SpecialtyResponse;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.SpecialtyMapper;
import com.mediq.scheduling.repository.SpecialtyRepository;
import com.mediq.scheduling.validation.SpecialtyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;
    private final SpecialtyValidator specialtyValidator;

    @Override
    @Transactional
    public SpecialtyResponse createSpecialty(CreateSpecialtyRequest request) {
        specialtyValidator.validateCreate(request);

        String trimmedName = request.getName().trim();
        if (specialtyRepository.existsByName(trimmedName)) {
            throw new BusinessException(ErrorCode.SPECIALTY_NAME_ALREADY_EXISTS,
                    "Specialty with name '" + trimmedName + "' already exists");
        }

        Specialty specialty = Specialty.builder()
                .name(trimmedName)
                .description(request.getDescription())
                .defaultSlotDuration(request.getDefaultSlotDuration() != null ? request.getDefaultSlotDuration() : 15)
                .status(SpecialtyStatus.ACTIVE)
                .build();

        Specialty savedSpecialty = specialtyRepository.save(specialty);
        return specialtyMapper.toSpecialtyResponse(savedSpecialty);
    }

    @Override
    public SpecialtyResponse getSpecialtyById(UUID specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIALTY_NOT_FOUND,
                        "Specialty not found with ID: " + specialtyId));
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    @Override
    public List<SpecialtyResponse> getAllSpecialties(SpecialtyStatus status) {
        List<Specialty> specialties = (status != null)
                ? specialtyRepository.findByStatus(status)
                : specialtyRepository.findAll();
        return specialtyMapper.toSpecialtyResponseList(specialties);
    }

    @Override
    @Transactional
    public SpecialtyResponse updateSpecialty(UUID specialtyId, UpdateSpecialtyRequest request) {
        specialtyValidator.validateUpdate(request);

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIALTY_NOT_FOUND,
                        "Specialty not found with ID: " + specialtyId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String trimmedName = request.getName().trim();
            if (specialtyRepository.existsByNameAndSpecialtyIdNot(trimmedName, specialtyId)) {
                throw new BusinessException(ErrorCode.SPECIALTY_NAME_ALREADY_EXISTS,
                        "Specialty with name '" + trimmedName + "' already exists");
            }
            specialty.setName(trimmedName);
        }

        if (request.getDescription() != null) {
            specialty.setDescription(request.getDescription());
        }

        if (request.getDefaultSlotDuration() != null) {
            specialty.setDefaultSlotDuration(request.getDefaultSlotDuration());
        }

        if (request.getStatus() != null) {
            specialty.setStatus(request.getStatus());
        }

        Specialty updatedSpecialty = specialtyRepository.save(specialty);
        return specialtyMapper.toSpecialtyResponse(updatedSpecialty);
    }
}
