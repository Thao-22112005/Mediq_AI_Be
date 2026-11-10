package com.mediqai.auth.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PatientClient {

    private final RestClient restClient;

    public void createPatient(
            Long userId,
            String fullName,
            String email,
            String phone
    ) {

        restClient.post()
                .uri("http://localhost:8083/api/v1/patients/internal")
                .body(
                        new CreatePatientRequest(
                                userId,
                                fullName,
                                email,
                                phone
                        )
                )
                .retrieve()
                .toBodilessEntity();


    }

    private record CreatePatientRequest(
            Long userId,
            String fullName,
            String email,
            String phone
    ) {
    }

    public void deletePatient(Long userId) {

        restClient.delete()
                .uri(
                        "http://localhost:8083/api/v1/patients/internal/user/{userId}",
                        userId
                )
                .retrieve()
                .toBodilessEntity();
    }
}