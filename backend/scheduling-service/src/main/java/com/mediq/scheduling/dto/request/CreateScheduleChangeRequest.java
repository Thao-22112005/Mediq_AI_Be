package com.mediq.scheduling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScheduleChangeRequest {

    private UUID scheduleId;
    private UUID doctorId;
    private LocalTime requestedStartTime;
    private LocalTime requestedEndTime;
    private UUID requestedRoomId;
    private String reason;
}
