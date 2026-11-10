package com.mediqai.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    private Long userId;

    private String fullName;

    private String phone;

    private String gender;

    private String dateOfBirth;

    private String address;

    private String bloodType;

    private String medicalHistory;

    private String avatarUrl;
}