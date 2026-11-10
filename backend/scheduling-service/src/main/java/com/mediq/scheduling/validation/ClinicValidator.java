package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateClinicRequest;
import com.mediq.scheduling.dto.request.UpdateClinicRequest;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ClinicValidator {

    public void validateCreate(CreateClinicRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Clinic code is required and cannot be blank");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Clinic name is required and cannot be blank");
        }
    }

    public void validateUpdate(UpdateClinicRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Clinic name cannot be blank");
        }
    }
}
