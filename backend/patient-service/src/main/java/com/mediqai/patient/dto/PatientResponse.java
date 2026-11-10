package com.mediqai.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;

    private Long userId;

    private String patientCode;

    private String fullName;

    private String email;

    private String phone;

    private String gender;

    private String dateOfBirth;

    private Integer age;

    private String address;

    private String bloodType;

    private String medicalHistory;

    private String avatarUrl;

    private String symptoms;

    private String status;
}