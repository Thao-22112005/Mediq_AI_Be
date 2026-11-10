package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;

import java.util.List;
import java.util.UUID;

public interface ScheduleChangeRequestService {

    ScheduleChangeRequestResponse createRequest(CreateScheduleChangeRequest request);

    ScheduleChangeRequestResponse getRequestById(UUID requestId);

    List<ScheduleChangeRequestResponse> listRequests(UUID doctorId, ScheduleChangeRequestStatus status);

    ScheduleChangeRequestResponse approveRequest(UUID requestId, ReviewScheduleChangeRequest request);

    ScheduleChangeRequestResponse rejectRequest(UUID requestId, ReviewScheduleChangeRequest request);

    ScheduleChangeRequestResponse cancelRequest(UUID requestId);
}
