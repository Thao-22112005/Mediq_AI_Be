package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateSpecialtyRequest;
import com.mediq.scheduling.dto.request.UpdateSpecialtyRequest;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyValidator {

    public void validateCreate(CreateSpecialtyRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty name is required and cannot be blank");
        }
        if (request.getDefaultSlotDuration() != null && request.getDefaultSlotDuration() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Default slot duration must be greater than 0");
        }
    }

    public void validateUpdate(UpdateSpecialtyRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty name cannot be blank");
        }
        if (request.getDefaultSlotDuration() != null && request.getDefaultSlotDuration() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Default slot duration must be greater than 0");
        }
    }
}
