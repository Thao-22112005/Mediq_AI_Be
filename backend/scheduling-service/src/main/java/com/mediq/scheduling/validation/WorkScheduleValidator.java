package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateWorkScheduleRequest;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkScheduleValidator {

    public void validateCreate(CreateWorkScheduleRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor ID is required");
        }
        if (request.getClinicId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Clinic ID is required");
        }
        if (request.getSpecialtyId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty ID is required");
        }
        if (request.getRoomId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Room ID is required");
        }
        if (request.getDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Schedule date is required");
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.PAST_SCHEDULE_NOT_ALLOWED, "Cannot create work schedule in the past");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Start time must be strictly before end time");
        }
    }
}
