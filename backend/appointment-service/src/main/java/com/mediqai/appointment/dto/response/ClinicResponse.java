package com.mediqai.appointment.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicResponse {

    private UUID clinicId;

    private String code;

    private String name;

    private String address;

    private String phone;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}