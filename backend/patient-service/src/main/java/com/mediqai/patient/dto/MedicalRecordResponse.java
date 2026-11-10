package com.mediqai.patient.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MedicalRecordResponse {

    private Long id;

    private Long patientId;

    private String symptoms;

    private String diagnosis;

    private String treatment;

    private LocalDate recordDate;

    private String status;
}