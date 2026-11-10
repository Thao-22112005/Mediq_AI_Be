package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateRoomRequest;
import com.mediq.scheduling.dto.request.UpdateRoomRequest;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoomValidator {

    public void validateCreate(UUID clinicId, CreateRoomRequest request) {
        if (clinicId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Clinic ID is required");
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Room code is required and cannot be blank");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Room name is required and cannot be blank");
        }
        if (request.getSpecialtyId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty ID is required for room configuration (BR-CLINIC-003)");
        }
    }

    public void validateUpdate(UpdateRoomRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Room name cannot be blank");
        }
    }
}
