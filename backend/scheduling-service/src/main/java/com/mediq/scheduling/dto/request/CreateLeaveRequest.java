package com.mediq.scheduling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeaveRequest {

    private UUID doctorId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;
}
