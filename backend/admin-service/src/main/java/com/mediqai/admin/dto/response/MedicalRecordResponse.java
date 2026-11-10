package com.mediqai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {

    private Long id;

    private Long patientId;

    private String symptoms;

    private String diagnosis;

    private String treatment;

    private String recordDate;

    private String status;
}