package com.mediqai.patient.dto;

import lombok.Data;

@Data
public class InternalPatientRequest {

    private Long userId;

    private String fullName;

    private String email;

    private String phone;
}