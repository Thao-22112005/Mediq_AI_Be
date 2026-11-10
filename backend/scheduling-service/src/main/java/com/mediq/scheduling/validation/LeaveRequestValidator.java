package com.mediq.scheduling.validation;

import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.entity.LeaveRequest;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LeaveRequestValidator {

    public void validateCreate(CreateLeaveRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Doctor ID is required");
        }
        if (request.getStartDateTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Start date time is required");
        }
        if (request.getEndDateTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "End date time is required");
        }
        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Start date time must be before end date time");
        }
        if (request.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAST_LEAVE_NOT_ALLOWED, "Cannot request leave in the past");
        }
    }

    public void validateReview(ReviewLeaveRequest request, LeaveRequest leaveRequest) {
        if (request == null || request.getReviewedBy() == null || request.getReviewedBy().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Reviewer information (reviewedBy) is required");
        }

        // BR-LEAVE-003: Doctor cannot self-approve leave request
        if (request.getReviewedBy().trim().equalsIgnoreCase(leaveRequest.getDoctorId().toString())) {
            throw new BusinessException(ErrorCode.DOCTOR_CANNOT_APPROVE_OWN_LEAVE,
                    "Doctor cannot review or approve their own leave request");
        }

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_LEAVE_STATUS_TRANSITION,
                    "Only PENDING leave requests can be reviewed (current status: " + leaveRequest.getStatus() + ")");
        }
    }

    public void validateCancel(LeaveRequest leaveRequest) {
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_LEAVE_STATUS_TRANSITION,
                    "Only PENDING leave requests can be cancelled (current status: " + leaveRequest.getStatus() + ")");
        }
    }
}
