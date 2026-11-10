package com.mediqai.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordRequest {

    private Long patientId;

    private String symptoms;

    private String diagnosis;

    private String treatment;

    private String recordDate;

    private String status;
}