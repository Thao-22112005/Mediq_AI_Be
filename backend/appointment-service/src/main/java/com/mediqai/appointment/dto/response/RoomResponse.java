package com.mediqai.appointment.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private UUID roomId;

    private UUID clinicId;

    private UUID specialtyId;

    private String code;

    private String name;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}