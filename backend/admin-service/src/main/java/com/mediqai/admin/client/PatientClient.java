package com.mediqai.admin.client;

import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.request.MedicalRecordRequest;
import com.mediqai.admin.dto.response.PatientResponse;
import com.mediqai.admin.dto.response.MedicalRecordResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PatientClient {

    private final RestClient restClient;

    public PatientClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // =========================================================
    // PATIENT
    // =========================================================

    // GET ALL PATIENTS
    public List<PatientResponse> getAllPatients(
            String token
    ) {

        return restClient.get()
                .uri(
                        "http://localhost:8083/api/v1/patients"
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<PatientResponse>
                                >() {}
                );
    }

    // GET PATIENT BY ID
    public PatientResponse getPatientById(
            Long id,
            String token
    ) {

        return restClient.get()
                .uri(
                        "http://localhost:8083/api/v1/patients/{id}",
                        id
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(PatientResponse.class);
    }

    // UPDATE PATIENT
    public PatientResponse updatePatient(
            Long id,
            PatientRequest request,
            String token
    ) {

        return restClient.put()
                .uri(
                        "http://localhost:8083/api/v1/patients/{id}",
                        id
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PatientResponse.class);
    }

    // =========================================================
    // MEDICAL RECORD
    // =========================================================

    // GET ALL MEDICAL RECORDS OF PATIENT
    public List<MedicalRecordResponse> getMedicalRecords(
            Long patientId,
            String token
    ) {

        return restClient.get()
                .uri(
                        "http://localhost:8083/api/v1/medical-records/patient/{patientId}",
                        patientId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<MedicalRecordResponse>
                                >() {}
                );
    }

    // UPDATE MEDICAL RECORD
    public MedicalRecordResponse updateMedicalRecord(
            Long id,
            MedicalRecordRequest request,
            String token
    ) {

        return restClient.put()
                .uri(
                        "http://localhost:8083/api/v1/medical-records/{id}",
                        id
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MedicalRecordResponse.class);
    }
}