package com.mediqai.appointment.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class DoctorResponse {

    private UUID id;
    private String fullName;
}
