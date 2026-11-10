package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private UUID roomId;

    private UUID clinicId;

    private UUID specialtyId;

    private String code;

    private String name;

    private String floor;

    private String area;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}