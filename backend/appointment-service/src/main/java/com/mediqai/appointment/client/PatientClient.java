package com.mediqai.appointment.client;

import com.mediqai.appointment.dto.response.PatientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PatientClient {

    private final RestClient restClient;

    public PatientResponse getMyPatient(String token) {

        System.out.println("========== PATIENT CLIENT ==========");
        System.out.println("Token length: " + (token == null ? "NULL" : token.length()));
        System.out.println("Token starts with Bearer: "
                + (token != null && token.startsWith("Bearer ")));

        return restClient.get()
                .uri("http://localhost:8083/api/v1/patients/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(PatientResponse.class);
    }


    public PatientResponse getPatientById(Long patientId) {
        return restClient.get()
                .uri("http://localhost:8083/api/v1/patients/internal/{id}", patientId)
                .retrieve()
                .body(PatientResponse.class);
    }
}