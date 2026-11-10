package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.ScheduleChangeRequest;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ScheduleChangeRequestValidator {

    public void validateCreate(
            CreateScheduleChangeRequest request,
            WorkSchedule schedule,
            Room requestedRoom,
            boolean hasActivePendingRequest
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Schedule ID is required");
        }
        if (request.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor ID is required");
        }
        if (request.getRequestedStartTime() == null || request.getRequestedEndTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Requested start and end times are required");
        }
        if (!request.getRequestedStartTime().isBefore(request.getRequestedEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Requested start time must be before end time");
        }
        if (schedule == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "Work schedule not found");
        }
        if (!schedule.getDoctorId().equals(request.getDoctorId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor ID does not match work schedule doctor");
        }
        if (schedule.getStatus() == WorkScheduleStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_CANCELLED, "Cannot change a cancelled work schedule");
        }
        if (hasActivePendingRequest) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Work schedule already has an active pending change request");
        }
        if (request.getRequestedRoomId() != null) {
            if (requestedRoom == null) {
                throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, "Requested room not found");
            }
            if (!requestedRoom.getClinic().getClinicId().equals(schedule.getClinic().getClinicId())) {
                throw new BusinessException(ErrorCode.ROOM_CLINIC_MISMATCH, "Requested room does not belong to the schedule clinic");
            }
            if (!requestedRoom.getSpecialty().getSpecialtyId().equals(schedule.getSpecialty().getSpecialtyId())) {
                throw new BusinessException(ErrorCode.ROOM_SPECIALTY_MISMATCH, "Requested room does not match the schedule specialty");
            }
        }
    }

    public void validateReview(ScheduleChangeRequest request, ReviewScheduleChangeRequest reviewRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND, "Schedule change request not found");
        }
        if (request.getStatus() != ScheduleChangeRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_CHANGE_STATUS_TRANSITION,
                    "Only PENDING schedule change requests can be reviewed (current status: " + request.getStatus() + ")");
        }
        if (reviewRequest == null || reviewRequest.getReviewedBy() == null || reviewRequest.getReviewedBy().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ReviewedBy is required");
        }
        if (reviewRequest.getReviewedBy().equalsIgnoreCase(request.getDoctorId().toString())) {
            throw new BusinessException(ErrorCode.DOCTOR_CANNOT_APPROVE_OWN_SCHEDULE_CHANGE,
                    "Doctor cannot approve or reject their own schedule change request");
        }
    }

    public void validateCancel(ScheduleChangeRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND, "Schedule change request not found");
        }
        if (request.getStatus() != ScheduleChangeRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_CHANGE_STATUS_TRANSITION,
                    "Only PENDING schedule change requests can be cancelled (current status: " + request.getStatus() + ")");
        }
    }
}
