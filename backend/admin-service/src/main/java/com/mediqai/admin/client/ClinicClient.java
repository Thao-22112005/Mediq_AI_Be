
        package com.mediqai.admin.client;

import com.mediqai.admin.dto.response.ClinicResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class ClinicClient {

    @Value("${services.scheduling.url}")
    private String schedulingUrl;

    private RestClient createClient() {
        return RestClient.create(schedulingUrl);
    }

    public List<ClinicResponse> getAllClinics(
            String status,
            String token
    ) {
        return createClient()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/clinics");

                    if (status != null && !status.isBlank()) {
                        uriBuilder.queryParam("status", status);
                    }

                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ClinicResponse>>() {});
    }

    public ClinicResponse getClinicById(
            UUID clinicId,
            String token
    ) {
        return createClient()
                .get()
                .uri("/clinics/{id}", clinicId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ClinicResponse.class);
    }
}

