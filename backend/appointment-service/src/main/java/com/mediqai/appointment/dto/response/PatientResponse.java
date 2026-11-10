package com.mediqai.appointment.dto.response;

import lombok.Data;

@Data
public class PatientResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String phone;
}