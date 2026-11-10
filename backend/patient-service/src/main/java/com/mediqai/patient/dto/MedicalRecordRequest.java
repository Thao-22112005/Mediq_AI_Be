package com.mediqai.patient.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicalRecordRequest {

    private Long patientId;

    private String symptoms;

    private String diagnosis;

    private String treatment;

    private LocalDate recordDate;

    private String status;
}