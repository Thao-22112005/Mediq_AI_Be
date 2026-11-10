package com.mediqai.admin.client;

import com.mediqai.admin.dto.response.AdminAppointmentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class AppointmentClient {

    private final RestClient restClient;

    @Value("${services.appointment.url}")
    private String appointmentServiceUrl;

    public AppointmentClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<AdminAppointmentResponse> getAppointments(
            LocalDate date,
            String status,
            UUID doctorId,
            Long patientId,
            String token
    ) {
        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromUriString(appointmentServiceUrl)
                        .path("/appointments");

        if (date != null) {
            builder.queryParam("date", date);
        }

        if (status != null && !status.isBlank()) {
            builder.queryParam("status", status);
        }

        if (doctorId != null) {
            builder.queryParam("doctorId", doctorId);
        }

        if (patientId != null) {
            builder.queryParam("patientId", patientId);
        }

        URI uri = builder.build().encode().toUri();

        List<AdminAppointmentResponse> result = restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<
                        List<AdminAppointmentResponse>>() {});

        return result == null ? List.of() : result;
    }

    public AdminAppointmentResponse getAppointmentById(
            UUID appointmentId,
            String token
    ) {
        return restClient.get()
                .uri(appointmentServiceUrl + "/appointments/{id}",
                        appointmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(AdminAppointmentResponse.class);
    }
}