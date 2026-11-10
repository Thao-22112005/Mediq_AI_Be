package com.mediq.scheduling.dto.response;

import java.util.List;

public record ApprovedLeaveResult(
        LeaveRequestResponse leaveRequest,
        List<AffectedScheduleDto> affectedSchedules,
        int cancelledSchedulesCount,
        int schedulesWithAppointmentsCount,
        int affectedAppointmentsCount
) {}
