package com.mediqai.admin.client;

import com.mediqai.admin.dto.request.AddDoctorSpecialtyRequest;
import com.mediqai.admin.dto.request.CreateDoctorRequest;
import com.mediqai.admin.dto.request.UpdateDoctorRequest;
import com.mediqai.admin.dto.response.DoctorResponse;
import com.mediqai.admin.dto.response.DoctorSpecialtyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class DoctorClient {

    private final RestClient restClient;

    @Value("${services.doctor.url}")
    private String doctorServiceUrl;

    public DoctorClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    public List<DoctorResponse> getAllDoctors(
            String status,
            String token
    ) {

        String url = doctorServiceUrl + "/doctors";

        if (status != null && !status.isBlank()) {
            url += "?status=" + status;
        }

        return restClient.get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<DoctorResponse>
                                >() {}
                );
    }

    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    public DoctorResponse getDoctorById(
            UUID doctorId,
            String token
    ) {

        return restClient.get()
                .uri(
                        doctorServiceUrl + "/doctors/{id}",
                        doctorId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(DoctorResponse.class);
    }

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    public DoctorResponse createDoctor(
            CreateDoctorRequest request,
            String token
    ) {

        return restClient.post()
                .uri(
                        doctorServiceUrl + "/doctors"
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DoctorResponse.class);
    }

    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public DoctorResponse updateDoctor(
            UUID doctorId,
            UpdateDoctorRequest request,
            String token
    ) {

        return restClient.patch()
                .uri(
                        doctorServiceUrl + "/doctors/{id}",
                        doctorId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DoctorResponse.class);
    }

    // =========================================================
    // GET DOCTOR SPECIALTIES
    // =========================================================

    public List<DoctorSpecialtyResponse> getDoctorSpecialties(
            UUID doctorId,
            String token
    ) {

        return restClient.get()
                .uri(
                        doctorServiceUrl +
                                "/doctors/{id}/specialties",
                        doctorId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<DoctorSpecialtyResponse>
                                >() {}
                );
    }

    // =========================================================
    // ADD SPECIALTY
    // =========================================================

    public DoctorSpecialtyResponse addSpecialty(
            UUID doctorId,
            AddDoctorSpecialtyRequest request,
            String token
    ) {

        return restClient.post()
                .uri(
                        doctorServiceUrl +
                                "/doctors/{id}/specialties",
                        doctorId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DoctorSpecialtyResponse.class);
    }

    // =========================================================
    // REMOVE SPECIALTY
    // =========================================================

    public void removeSpecialty(
            UUID doctorId,
            UUID specialtyId,
            String token
    ) {

        restClient.delete()
                .uri(
                        doctorServiceUrl +
                                "/doctors/{id}/specialties/{specialtyId}",
                        doctorId,
                        specialtyId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .toBodilessEntity();
    }
}