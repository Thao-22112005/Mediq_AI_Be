package com.mediqai.appointment.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {

    private UUID slotId;

    private UUID scheduleId;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}