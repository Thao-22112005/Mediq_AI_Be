package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {

    private UUID specialtyId;

    private String name;

    private String description;

    private String status;

    private Integer defaultSlotDuration;

    private Instant createdAt;

    private Instant updatedAt;
}