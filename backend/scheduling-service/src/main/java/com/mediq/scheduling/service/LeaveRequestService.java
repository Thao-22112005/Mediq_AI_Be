package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.LeaveRequestStatus;

import java.util.List;
import java.util.UUID;

public interface LeaveRequestService {

    LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request);

    LeaveRequestResponse getLeaveRequestById(UUID leaveRequestId);

    List<LeaveRequestResponse> listLeaveRequests(UUID doctorId, LeaveRequestStatus status);

    com.mediq.scheduling.dto.response.ApprovedLeaveResult approveLeaveRequest(UUID leaveRequestId, ReviewLeaveRequest request);

    com.mediq.scheduling.dto.response.ApprovedLeaveResult getAffectedSchedules(UUID leaveRequestId);

    LeaveRequestResponse rejectLeaveRequest(UUID leaveRequestId, ReviewLeaveRequest request);

    LeaveRequestResponse cancelLeaveRequest(UUID leaveRequestId);
}
