package com.mediq.doctor.validation;

import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.request.UpdateDoctorRequest;
import com.mediq.doctor.exception.BusinessException;
import com.mediq.doctor.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DoctorValidator {

    public void validateCreate(CreateDoctorRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getUserId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "User ID is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor full name is required");
        }
        if (request.getFullName().length() > 255) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor full name must not exceed 255 characters");
        }


        if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
            validateInitialSpecialties(request.getSpecialties());
        }
    }

    public void validateUpdate(UpdateDoctorRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getFullName() != null) {
            if (request.getFullName().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor full name cannot be blank");
            }
            if (request.getFullName().length() > 255) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor full name must not exceed 255 characters");
            }
        }
    }

    public void validateAddSpecialty(AddDoctorSpecialtyRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getSpecialtyId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty ID is required");
        }
    }

    /**
     * Enforces BR-DOCTOR-001:
     * - Doctor can have multiple specialties.
     * - Doctor has at most one primary specialty.
     * - No duplicate specialties.
     */
    public void validateInitialSpecialties(List<CreateDoctorRequest.InitialSpecialtyRequest> specialties) {
        Set<UUID> seenSpecialtyIds = new HashSet<>();
        long primaryCount = 0;

        for (CreateDoctorRequest.InitialSpecialtyRequest specialty : specialties) {
            if (specialty.getSpecialtyId() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty ID in list cannot be null");
            }
            if (!seenSpecialtyIds.add(specialty.getSpecialtyId())) {
                throw new BusinessException(ErrorCode.SPECIALTY_ALREADY_ASSIGNED, 
                        "Duplicate specialty ID found in request: " + specialty.getSpecialtyId());
            }
            if (specialty.isPrimary()) {
                primaryCount++;
            }
        }

        if (primaryCount > 1) {
            throw new BusinessException(ErrorCode.PRIMARY_SPECIALTY_ALREADY_EXISTS, 
                    "A doctor can have at most one primary specialty");
        }
    }
}
