package com.mediqai.appointment.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialtyResponse {

    private UUID specialtyId;

    private String name;

    private String description;

    private String status;

    private Integer defaultSlotDuration;

    private Instant createdAt;

    private Instant updatedAt;
}