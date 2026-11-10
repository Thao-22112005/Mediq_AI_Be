package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateWorkScheduleRequest;
import com.mediq.scheduling.dto.response.WorkScheduleResponse;
import com.mediq.scheduling.entity.WorkScheduleStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WorkScheduleService {

    WorkScheduleResponse createWorkSchedule(CreateWorkScheduleRequest request);

    WorkScheduleResponse getWorkScheduleById(UUID scheduleId);

    List<WorkScheduleResponse> getWorkSchedules(UUID doctorId, UUID clinicId, UUID roomId,LocalDate date, WorkScheduleStatus status);

    WorkScheduleResponse approveWorkSchedule(UUID scheduleId);

    WorkScheduleResponse cancelWorkSchedule(UUID scheduleId);
}
